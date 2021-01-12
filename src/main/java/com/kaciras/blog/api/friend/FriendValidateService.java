package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.api.RedisOperationsBuilder;
import com.kaciras.blog.api.notice.NoticeService;
import com.kaciras.blog.infra.RedisExtensions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 定时扫描对方的网站，检查是否嗝屁（默哀），以及单方面删除本站（为什么不跟人家做朋友了）。
 * 要启用检查，请将 app.validate-friend 设置为 true。
 *
 * <h2>安全性</h2>
 * 发送请求可能暴露服务器的地址，这种情况下可以通过 app.http-client.proxy 设置代理。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendValidateService {

	private final NoticeService noticeService;
	private final FriendRepository repository;

	private final Clock clock;
	private final FriendValidator validator;

	private RedisMap<String, ValidateRecord> validateMap;

	// 该方法不能是 private，因为私有方法隐含 final，导致无法被 mock，而 Autowired 又让它被测试容器调用，
	// 这导致实际的代码在 mock 测试中运行。
	@Autowired
	void setRedis(RedisOperationsBuilder builder) {
		validateMap = new DefaultRedisMap<>(builder.bindHash(RedisKeys.Friends.of("validate"), ValidateRecord.class));
	}

	/**
	 * 将一个友链加入验证列表中。
	 * <p>
	 * 【初次验证时间】
	 * 友链的最后有效时间以调用此方法的时间为准，不使用友链创建时间，避免友链域名更新后立即检查。
	 *
	 * @param friend 友链
	 */
	public void addForValidate(FriendLink friend) {
		var url = friend.url;
		validateMap.put(url.getHost(), new ValidateRecord(url, friend.friendPage, clock.instant(), 0));
	}

	/**
	 * 从验证列表中移除指定域名的友链。
	 *
	 * @param host 域名
	 */
	public void removeFromValidate(String host) {
		validateMap.remove(host);
	}

	/**
	 * 开始一次检查，从所有的记录中筛选出待检查的友链进行检查。
	 */
	public void startValidation() {
		var queue = new LinkedList<ValidateRecord>();
		var records = validateMap.values();

		records.stream().filter(this::shouldValidate).forEach(queue::addFirst);

		if (!queue.isEmpty()) {
			logger.info("共有{}个友链，本次检测{}个", records.size(), queue.size());
		}

		validateFriendsAsync(queue);
	}

	/**
	 * 判断友链是否需要检测，如果之前一直都正常则30天检查一次，上次无法访问则7天后再次检查。
	 *
	 * @param record 检测记录
	 * @return 如果需要检测则为true
	 */
	private boolean shouldValidate(ValidateRecord record) {
		var p = record.failed > 0 ? Duration.ofDays(7) : Duration.ofDays(30);
		return record.validate.plus(p).isBefore(clock.instant());
	}

	private void validateFriendsAsync(Queue<ValidateRecord> queue) {
		if (queue.isEmpty()) {
			return;
		}
		var record = queue.remove();
		var checkUrl = record.friendPage != null ? record.friendPage : record.url;

		validator.visit(checkUrl)
				.thenAccept(page -> this.handleResponse(record, page))
				.thenRun(() -> validateFriendsAsync(queue));
	}

	private void handleResponse(ValidateRecord record, FriendSitePage page) {
		record.validate = clock.instant();

		// 如果访问失败则7天后再次检测，连续4次（一个月）都失败的视为无法访问
		if (!page.isAlive()) {
			record.failed++;

			if (record.failed > 3) {
				report(FriendAccident.Type.Inaccessible, record, null);
				record.failed = 0;
			}
		} else {
			// 一旦访问成功就把失败次数归零
			record.failed = 0;

			// 有重定向直接报告
			if (page.getNewUrl() != null) {
				report(FriendAccident.Type.Moved, record, page.getNewUrl());
			}

			// 如果友链输入了互链检查地址则判断是否存在本站的链接
			if (record.friendPage != null && !page.hasMyLink()) {
				report(FriendAccident.Type.AbandonedMe, record, null);
			}
		}

		updateRecordEntry(record);
	}

	private void report(FriendAccident.Type type, ValidateRecord record, URI newUrl) {
		var friend = repository.findByHost(record.url.getHost());
		if (friend == null) {
			return; // 用户删除了友链产生不一致状态
		}
		noticeService.add(new FriendAccident(type, friend.name, friend.url, newUrl));
	}

	/**
	 * 更新友链的检查记录。
	 * 用了 Lua 脚本来实现 putIfExists，确保不会添加已删除的记录。
	 *
	 * @param record 新的记录
	 */
	private void updateRecordEntry(ValidateRecord record) {
		var host = record.url.getHost();
		RedisExtensions.hsetx(validateMap.getOperations(), validateMap.getKey(), host, record);
	}
}
