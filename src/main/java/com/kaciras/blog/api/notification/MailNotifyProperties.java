package com.kaciras.blog.api.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("app.mail-notify")
public final class MailNotifyProperties {

	public final String from;

	public final String address;

	public MailNotifyProperties(String from, String address) {
		this.from = from;
		this.address = address;
	}
}
