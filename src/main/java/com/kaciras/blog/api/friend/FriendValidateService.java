package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.api.notification.FriendAccident;
import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.infra.RedisExtensions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 定时扫描对方的网站，检查是否嗝屁（默哀），以及单方面删除本站（为什么不跟人家做朋友了）。
 * 要启用检查，请将 app.validate-friend 设置为 true。
 * <p>
 * 【安全性】
 * 发送请求可能暴露服务器的地址，这种情况下可以通过 app.http-client.proxy 设置代理。
 */
@RequiredArgsConstructor
@Service
public class FriendValidateService {

	private final NotificationService notificationService;
	private final FriendRepository repository;

	private final Clock clock;
	private final FriendValidator validator;

	private final TaskScheduler taskScheduler;

	private RedisMap<String, ValidateRecord> validateMap;

	@Value("${app.validate-friend}")
	private boolean enable;

	@Autowired
	private void setRedis(RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		var hvs = new Jackson2JsonRedisSerializer<>(ValidateRecord.class);
		hvs.setObjectMapper(objectMapper);

		var validate = new RedisTemplate<String, Object>();
		validate.setConnectionFactory(redisFactory);
		validate.setDefaultSerializer(RedisSerializer.string());
		validate.setHashValueSerializer(hvs);
		validate.afterPropertiesSet();

		validateMap = new DefaultRedisMap<>(RedisKeys.Friends.of("validate"), validate);
	}

	@PostConstruct
	private void init() {
		if (enable) {
			taskScheduler.scheduleAtFixedRate(this::startValidation, Duration.ofDays(1));
		}
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
	 * 触发检测，将从所有的记录中筛选出待检查的友链进行检查。
	 * <p>
	 * 可以搞个定时任务来调用此方法。
	 */
	public void startValidation() {
		var queue = new LinkedList<ValidateRecord>();
		validateMap.values().stream().filter(this::shouldValidate).forEach(queue::addFirst);
		validateFriendsAsync(queue);
	}

	/**
	 * 判断友链是否需要检测，如果之前一直都正常则30天检查一次，否则7天后再次检查。
	 *
	 * @param record 检测记录
	 * @return 如果需要检测则为true
	 */
	private boolean shouldValidate(ValidateRecord record) {
		var p = record.failed > 0 ? Duration.ofDays(7) : Duration.ofDays(30);
		return record.validate.plus(p).isAfter(clock.instant());
	}

	private void validateFriendsAsync(Queue<ValidateRecord> queue) {
		if (queue.isEmpty()) {
			return;
		}
		var record = queue.remove();
		var checkUrl = record.friendPage != null ? record.friendPage : record.url;

		validator.visit(checkUrl).thenAccept(page -> this.handleResponse(record, page));
	}

	private void handleResponse(ValidateRecord record, FriendSitePage page) {
		record.validate = clock.instant();

		if (!page.isAlive()) {
			record.failed++;

			if (record.failed > 3) {
				report(FriendAccident.Type.Inaccessible, record, null);
				record.failed = 0;
			}
		} else {
			record.failed = 0;

			if (page.getNewUrl() != null) {
				report(FriendAccident.Type.Moved, record, page.getNewUrl());
			}

			if (record.friendPage != null && !page.hasMyLink()) {
				report(FriendAccident.Type.AbandonedMe, record, null);
			}
		}

		updateRecordEntry(record);
	}

	private void report(FriendAccident.Type type, ValidateRecord record, URI newUrl) {
		var friend = repository.get(record.url.getHost());
		if (friend == null) {
			return; // 不会遇到正好刚刚删除的情况吧
		}
		notificationService.reportFriend(type, friend, record.validate, newUrl);
	}

	/**
	 * 保存检查记录。用了 Lua 脚本来实现 putIfExists，确保不会添加已删除的记录。
	 *
	 * @param record 新的记录
	 */
	private void updateRecordEntry(ValidateRecord record) {
		var host = record.url.getHost();
		RedisExtensions.hsetx(validateMap.getOperations(), validateMap.getKey(), host, record);
	}
}
