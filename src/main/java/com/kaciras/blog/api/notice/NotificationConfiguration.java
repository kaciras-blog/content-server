package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

@EnableConfigurationProperties(MailNotifyProperties.class)
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class NotificationConfiguration {

	private final MailNotifyProperties properties;

	@ConditionalOnProperty(prefix = "app.mail-notify", name = "from")
	@Bean
	public MailService mailService(JavaMailSender mailSender) {
		return new MailService(mailSender, properties.from, properties.address);
	}

	@Bean
	public NotificationService notificationService(
			ObjectMapper objectMapper,
			RedisTemplate<String, byte[]> redis,
			Optional<MailService> mailService) {
		return new NotificationService(redis.opsForList(), objectMapper, mailService.orElse(null));
	}
}
