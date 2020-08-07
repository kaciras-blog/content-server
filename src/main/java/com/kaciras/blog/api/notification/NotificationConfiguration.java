package com.kaciras.blog.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.util.Optional;

@EnableConfigurationProperties(MailNotifyProperties.class)
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class NotificationConfiguration {

	private final MailNotifyProperties properties;

	@ConditionalOnProperty(prefix = "app.mail-notify", name = "from")
	@Bean
	public MailService mailService(JavaMailSender mailSender) throws IOException {
		var freeMaker = new Configuration(Configuration.VERSION_2_3_30);
		freeMaker.setDirectoryForTemplateLoading(new ClassPathResource("templates").getFile());
		freeMaker.setDefaultEncoding("utf-8");

		return new MailService(mailSender, freeMaker, properties.from);
	}

	@Bean
	public NotificationService notificationService(RedisConnectionFactory factory,
												   ObjectMapper objectMapper,
												   Optional<MailService> mailService) {
		if (properties.address != null && mailService.isEmpty()) {
			throw new BeanCreationException("app.mail-notify 需要配置邮件 spring.mail");
		}
		return new NotificationService(factory, objectMapper, properties.address, mailService.orElse(null));
	}
}
