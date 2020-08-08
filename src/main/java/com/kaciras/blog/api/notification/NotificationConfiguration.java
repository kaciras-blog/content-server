package com.kaciras.blog.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import java.util.Optional;

@EnableConfigurationProperties(MailNotifyProperties.class)
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class NotificationConfiguration {

	private final MailNotifyProperties properties;

	@ConditionalOnProperty(prefix = "app.mail-notify", name = "from")
	@Bean
	public MailService mailService(JavaMailSender mailSender) {
		var freeMarker = new Configuration(Configuration.VERSION_2_3_30);

		/*
		 * 这里不能用 new ClassPathResource(...).getFile()，因为默认的 FileTemplateLoader 不能读取JAr包内的文件，
		 * 如果用它的话则会报错 FileNotFoundException。
		 *
		 * 根据 Spring 的代码：
		 * org.springframework.ui.freemarker.FreeMarkerConfigurationFactory#getTemplateLoaderForPath
		 *
		 * 此处可以用 Spring 的 SpringTemplateLoader 来加载JAR包内的模板。
		 */
		freeMarker.setTemplateLoader(new SpringTemplateLoader(new DefaultResourceLoader(), "classpath:/templates"));
		freeMarker.setDefaultEncoding("utf-8");

		return new MailService(mailSender, freeMarker, properties.from);
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
