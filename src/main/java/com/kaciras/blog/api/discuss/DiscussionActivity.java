package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kaciras.blog.api.notice.Activity;
import com.kaciras.blog.api.notice.ActivityType;
import com.kaciras.blog.api.notice.MailService;
import com.kaciras.blog.api.user.User;
import com.kaciras.blog.infra.principal.WebPrincipal;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
final class DiscussionActivity implements Activity {

	/** 评论所在的页面地址和标题 */
	private String url;
	private String title;

	private DiscussionState state;
	private int floor;

	private int topicFloor;
	private int nestFloor;

	/** 内容预览 */
	private String preview;

	@JsonIgnore
	private User user;

	// 仅当顶层评论时为 null
	@JsonIgnore
	private User parentUser;

	@JsonIgnore
	private String email;

	/**
	 * 父评论者填写的邮箱
	 */
	@JsonIgnore
	private String parentEmail;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DISCUSSION;
	}

	@Override
	public boolean isAdminMessage() {
		return user.getId() != WebPrincipal.ADMIN_ID;
	}

	@Override
	public void sendMail(boolean clear, MailService sender) {
		// 给站长的邮件，仅提示有新回复，具体内容去后台看。
		if (clear && user.getId() != WebPrincipal.ADMIN_ID) {
			sender.sendToAdmin("有新评论啦",
					sender.interpolate("NewDiscussion.html", Map.of()));
		}

		// 登录了就不支持匿名邮箱，因为本来就是为了第三方验证才搞得登录。
		var senderEmail = email;
		if (user.getId() != WebPrincipal.ANONYMOUS_ID) {
			senderEmail = user.getEmail();
		}
		var receiver = parentEmail;
		if (parentUser.getId() != WebPrincipal.ANONYMOUS_ID) {
			receiver = parentUser.getEmail();
		}

		if (receiver == null || receiver.equals(senderEmail)) {
			return; // 邮件地址相同视为回复自己，不发送通知。
		}

		var html = sender.interpolate("ReplyToast.html", Map.of(
				"title", title,
				"url", url,
				"content", preview,
				"floor", floor,
				"nest", topicFloor,
				"nestFloor", nestFloor
		));
		sender.send(receiver, "新回复", html);
	}
}
