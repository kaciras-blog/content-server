package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kaciras.blog.api.notice.Activity;
import com.kaciras.blog.api.notice.ActivityType;
import com.kaciras.blog.api.notice.MailService;
import com.kaciras.blog.api.user.User;
import com.kaciras.blog.infra.principal.WebPrincipal;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
final class DiscussionActivity implements Activity {

	/** 评论所在的页面地址和标题 */
	private String url;
	private String title;

	private DiscussionState state;
	private int floor;
	private int nestFloor;

	/** 内容预览 */
	private String preview;

	@JsonIgnore
	private Discussion nestRoot;

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
			var html = "<p>详情请前往控制台查看哦</p><p>如果还要接收邮件，请清除全部评论通知</p>";
			sender.sendToAdmin("博客有新评论啦", html);
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

		var model = new HashMap<String, Object>();
		model.put("title", title);
		model.put("url", url);
		model.put("content", preview);
		model.put("floor", floor);
		model.put("nest", nestRoot.getNestFloor());
		model.put("nestFloor", nestFloor);

		var html = sender.template("ReplyToast.html", model);
		sender.send(receiver, "新回复 - Kaciras Blog", html);
	}
}
