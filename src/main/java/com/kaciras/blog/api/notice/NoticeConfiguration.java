package com.kaciras.blog.api.notice;

import com.kaciras.blog.infra.RedisOperationsBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.mail.javamail.JavaMailSender;

/*
 * 通知一般用 Notification，但这词太长也不好看，于是就改成 Notice 反正个人项目意思差不多就行了。
 */
@EnableConfigurationProperties(MailNotifyProperties.class)
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NoticeConfiguration {

	private final MailNotifyProperties properties;

	@ConditionalOnProperty(prefix = "app.notice.mail", name = "from")
	@Bean
	public MailService mailService(JavaMailSender mailSender) {
		return new MailService(mailSender, properties.from, properties.admin);
	}

	@Bean
	public BoundListOperations<String, Notice> noticeRedisList(RedisOperationsBuilder redis) {
		return redis.bindList("notice", Notice.class);
	}
}
