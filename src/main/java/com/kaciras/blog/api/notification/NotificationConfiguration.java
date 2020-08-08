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

		/*
		 * 邮件内容也属于前端视图，但它由后端发送，所以只能选个后端模板库用了。
		 * 而且邮件环境不同于浏览器，没法复用前端项目代码。
		 *
		 * TODO：可否用模板生成 Markdown，然后再渲染成 HTML？这样做好像仍然需要模板引擎。
		 */
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
