package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.notice.HttpNotice;
import com.kaciras.blog.api.notice.MailNotice;
import com.kaciras.blog.api.notice.MailService;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
final class DiscussionActivity implements HttpNotice, MailNotice {

	/** 评论所在的页面地址和标题 */
	private String url;
	private String title;

	private int floor;
	private int treeFloor;

	/** 内容预览 */
	private String preview;

	/** 评论时间 */
	private Instant time;

	@Override
	public String getKind() {
		return "dz";
	}

	@Override
	public void sendMail(boolean clear, MailService sender) {
		if (!clear) {
			return;
		}
		// 邮件仅提示有新回复，具体内容去网站看
		var html = "<p>详情请前往控制台查看哦</p><p>如果还要接收邮件，请清除全部评论通知</p>";
		sender.sendToAdmin("博客有新评论", html);
	}
}
