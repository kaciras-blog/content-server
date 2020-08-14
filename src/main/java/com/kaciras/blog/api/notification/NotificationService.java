package com.kaciras.blog.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisOperationBuilder;
import com.kaciras.blog.api.article.Article;
import com.kaciras.blog.api.discuss.Discussion;
import com.kaciras.blog.api.friend.FriendLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.scheduling.annotation.Async;

import java.net.URI;
import java.time.Instant;

public class NotificationService {

	private final BoundListOperations<String, DiscussionActivity> discussions;
	private final BoundListOperations<String, FriendAccident> friends;

	private final MailService mailService;
	private final String adminAddress;

	@Value("${app.origin}")
	private String origin;

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
	public void reportFriend(FriendAccident.Type type, FriendLink friend, Instant time, URI newUrl) {
		friends.rightPush(new FriendAccident(type, friend.name, friend.url, newUrl, time));
	}

	@Async
	public void reportDiscussion(Discussion discussion, Discussion parent, Article article) {
		if (discussion.getUserId() == 2) {
			return; // 博主自己的评论就不用提醒了
		}

		var entry = new DiscussionActivity();
		entry.setFloor(discussion.getFloor());
		entry.setTime(discussion.getTime());

		if (parent != null) {
			entry.setParentFloor(parent.getFloor());
		}

		var content = discussion.getContent();
		var preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
		entry.setPreview(preview);

		/*
		 * 页面的组织是前端的事情，在前后分离的项目中不应该在这里构造URL。
		 * 通常来说，URL是要提交评论时当参数传过来保存的，但最初设计时未考虑到，所以还是把URL的构建写这了。
		 *
		 * TODO：下一版如果使用Node全栈，也许可以复用路由代码来确定URL
		 */
		if (discussion.getType() == 1) {
			entry.setTitle("关于博主");
			entry.setUrl(origin + "/about/blogger");
		} else {
			entry.setTitle(article.getTitle());
			entry.setUrl(String.format("%s/article/%d/%s", origin, article.getId(), article.getUrlTitle()));
		}

		var size = discussions.rightPush(entry);

		// 只有通知为空时才发邮件，因为未读消息发一个邮件提醒去控制台看就行了。
		if (size == 1 && adminAddress != null) {
			var html = "<p>详情请前往控制台查看哦</p><p>如果还要接收邮件，请清除全部评论通知</p>";
			mailService.send(adminAddress, "博客有新评论", html);
		}
	}
}
