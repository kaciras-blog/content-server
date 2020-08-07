package com.kaciras.blog.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisOperationBuilder;
import com.kaciras.blog.api.article.Article;
import com.kaciras.blog.api.discuss.Discussion;
import com.kaciras.blog.api.friend.FriendLink;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;

import java.time.Instant;

public class NotificationService {

	private final BoundListOperations<String, DiscussionActivity> discussions;
	private final BoundListOperations<String, FriendAccident> friends;

	private final MailService mailService;

	private final String adminAddress;

	public NotificationService(RedisConnectionFactory factory,
							   ObjectMapper objectMapper,
							   String adminAddress,
							   MailService mailService) {
		this.mailService = mailService;
		this.adminAddress = adminAddress;

		var builder = new RedisOperationBuilder(factory, objectMapper);
		friends = builder.bindList("notice:fr", FriendAccident.class);
		discussions = builder.bindList("notice:dz", DiscussionActivity.class);
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

	// 加上异步以便不干扰调用方的流程，如果出了异常也属于本模块的。
	@Async
	public void reportFriend(FriendLink friend, Instant time, FriendAccident.Type type) {
		var entry = new FriendAccident();
		entry.url = friend.url;
		entry.name = friend.name;
		entry.type = type;
		entry.time = time;
		friends.rightPush(entry);
	}

	@Async
	public void reportDiscussion(Discussion discussion, Discussion parent, Article article) {
		var entry = new DiscussionActivity();
		entry.floor = discussion.getFloor();
		entry.time = discussion.getTime();

		if (parent != null) {
			entry.parentFloor = parent.getFloor();
		}

		if (discussion.getContent().length() > 50) {
			entry.preview = discussion.getContent().substring(0, 50) + "...";
		} else {
			entry.preview = discussion.getContent();
		}

		// 要
		if (discussion.getType() == 1) {
			entry.url = "/about/blogger";
		} else {
			entry.url = String.format("/article/%d/%s", article.getId(), article.getUrlTitle());
		}

		discussions.rightPush(entry);

		if (adminAddress != null) {
			mailService.send(adminAddress, "博客有新评论", entry, "discussion-mail.ftl");
		}
	}
}
