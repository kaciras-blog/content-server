package com.kaciras.blog.api.notification;

import com.kaciras.blog.api.RedisOperationsBuilder;
import com.kaciras.blog.api.discuss.DiscussChannel;
import com.kaciras.blog.api.discuss.Discussion;
import com.kaciras.blog.api.friend.FriendLink;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;

import java.net.URI;
import java.time.Instant;

public class NotificationService {

	private final BoundListOperations<String, DiscussionActivity> discussions;
	private final BoundListOperations<String, FriendAccident> friends;

	private final MailService mailService;
	private final String adminAddress;

	public NotificationService(RedisOperationsBuilder redisBuilder,
							   String adminAddress,
							   MailService mailService) {
		this.mailService = mailService;
		this.adminAddress = adminAddress;

		friends = redisBuilder.bindList("notice:fr", FriendAccident.class);
		discussions = redisBuilder.bindList("notice:dz", DiscussionActivity.class);
	}

	public Notifications getAll() {
		var result = new Notifications();
		result.friends = friends.range(0, -1);
		result.discussions = discussions.range(0, -1);
		return result;
	}

	public void clear() {
		discussions.getOperations().unlink(discussions.getKey());
		friends.getOperations().unlink(friends.getKey());
	}

	// 加上异步以便不干扰调用方的流程，如果出了异常也只限于本模块。
	@Async
	public void reportFriend(FriendAccident.Type type, FriendLink friend, Instant time, URI newUrl) {
		friends.rightPush(new FriendAccident(type, friend.name, friend.url, newUrl, time));
	}

	@Async
	public void reportDiscussion(Discussion discussion, Discussion parent, DiscussChannel channel) {
		if (discussion.getUserId() == 2) {
			return; // 自己的评论就不用提醒了
		}

		var entry = new DiscussionActivity();
		entry.setChannelFloor(discussion.getChannelFloor());
		entry.setReplyFloor(discussion.getReplyFloor());
		entry.setTime(discussion.getTime());
		entry.setUrl(channel.getUrl());
		entry.setTitle(channel.getName());

		var content = discussion.getContent();
		var preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
		entry.setPreview(preview);

		if (parent != null) {
			entry.setParentFloor(parent.getReplyFloor());
		}

		var size = discussions.rightPush(entry);

		// 只有通知为空时才发邮件，更多内容自己去控制台看就行。
		// noinspection ConstantConditions
		if (size == 1 && adminAddress != null) {
			var html = "<p>详情请前往控制台查看哦</p><p>如果还要接收邮件，请清除全部评论通知</p>";
			mailService.send(adminAddress, "博客有新评论", html);
		}
	}
}
