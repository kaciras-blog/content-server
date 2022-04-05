package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kaciras.blog.api.notice.Activity;
import com.kaciras.blog.api.notice.ActivityType;
import com.kaciras.blog.api.notice.MailNotice;
import com.kaciras.blog.api.notice.MailService;
import com.kaciras.blog.api.user.User;
import com.kaciras.blog.infra.principal.WebPrincipal;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.function.Consumer;

@Getter
@Setter
final class DiscussionActivity implements Activity, MailNotice {

	/** 评论所在的页面地址和标题 */
	private String url;
	private String title;

	private DiscussionState state;
	private int floor;
	private int nestFloor;

	/** 内容预览 */
	private String preview;

	@JsonIgnore
	private User user;

	// 仅当顶层评论时为 null
	@JsonIgnore
	private User parentUser;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DISCUSSION;
	}

	@Override
	public void sendMail(boolean clear, MailService sender) {
		// 仅提示有新回复，具体内容去后台看
		if (clear && user.getId() != WebPrincipal.ADMIN_ID) {
			var html = "<p>详情请前往控制台查看哦</p><p>如果还要接收邮件，请清除全部评论通知</p>";
			sender.sendToAdmin("博客有新评论", html);
		}

		Consumer<String> sendReplyMail = email -> {
			var template = """
					<p>您在 <a href="%s">%s</a> 下的评论有新回复</p>
					<blockquote><pre>%s</pre></blockquote>
					""";
			var html =  String.format(template, url, title, preview);
			sender.send(email, "新回复 - Kaciras Blog", html);
		};

		// 给被回复者发邮件，如果它登录了且填了邮箱。
		Optional.ofNullable(parentUser)
				.filter(u -> u.getId() != user.getId())
				.map(User::getEmail)
				.ifPresent(sendReplyMail);
	}
}
