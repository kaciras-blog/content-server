package com.kaciras.blog.api.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@ConfigurationProperties("app.notice.mail")
public final class MailNotifyProperties {

	/**
	 * 邮件发送方的地址，要启用邮件功能必须设置此项
	 */
	public final String from;

	/**
	 * 接收全站消息提醒的邮箱，如果为 null 则不发送邮件
	 */
	public final String admin;
}
