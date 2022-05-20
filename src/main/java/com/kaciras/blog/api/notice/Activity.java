package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 表示一条动态，需要以各种方式（站内消息、邮件等）通知用户或管理员。
 * 为了简便，各种通知方式直接以方法的形式提供给子类重写，且子类将直接序列化作为站内信。
 *
 * @see NoticeService#notify(Activity)
 */
public interface Activity {

	@JsonIgnore
	ActivityType getActivityType();

	/**
	 * 该消息是否要显示在管理员的消息列表中，默认 true 表示需要显示。
	 */
	@JsonIgnore
	default boolean isAdminMessage() { return true; }

	/**
	 * 表示通知需要发送邮件的接口，默认的实现是空方法不发送邮件。
	 *
	 * 邮件属于 MVC 里的 View，是面向邮件客户端的视图，使用 HTML 格式。
	 */
	default void sendMail(boolean clear, MailService sender) {}
}
