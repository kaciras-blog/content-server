package com.kaciras.blog.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisOperationBuilder;
import com.kaciras.blog.api.article.ArticleRepository;
import com.kaciras.blog.api.discuss.Discussion;
import com.kaciras.blog.api.friend.FriendLink;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@RequiredArgsConstructor
@Repository
public class NotificationRepository {

	private final ArticleRepository articleRepository;

	private BoundListOperations<String, NewDiscussion> discussions;
	private BoundListOperations<String, FriendAccident> friends;

	@Autowired
	private void setRedis(RedisConnectionFactory factory, ObjectMapper objectMapper) {
		var builder = new RedisOperationBuilder(factory, objectMapper);

		friends = builder.bindList("notice:fr", FriendAccident.class);
		discussions = builder.bindList("notice:dz", NewDiscussion.class);
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
	public void addFriendRecord(FriendLink friend, Instant time, FriendAccident.Type type) {
		var entry = new FriendAccident();
		entry.url = friend.url;
		entry.name = friend.name;
		entry.type = type;
		entry.time = time;
		friends.rightPush(entry);
	}

	@Async
	public void addDiscussionRecord(Discussion discussion) {
		var entry = new NewDiscussion();
		entry.parent = discussion.getParent();
		entry.floor = discussion.getFloor();
		entry.time = discussion.getTime();

		if (discussion.getContent().length() > 50) {
			entry.preview = discussion.getContent().substring(0, 50) + "...";
		} else {
			entry.preview = discussion.getContent();
		}

		//
		if (discussion.getType() == 1) {
			entry.url = "/about/blogger";
		} else {
			var article = articleRepository.findById(discussion.getObjectId());
			entry.url = String.format("/article/%d/%s", article.getId(), article.getUrlTitle());
		}

		discussions.rightPush(entry);
	}
}
