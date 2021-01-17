package com.kaciras.blog.api.notice;

/**
 * 表示通知需要发送邮件的接口。
 *
 * 邮件的内容属于 MVC 里的 View，是面向邮件客户端的视图，使用 HTML 格式，所以邮件系统需要后端模板。
 */
public interface MailNotice {

	void sendMail(boolean clear, MailService sender);
}
