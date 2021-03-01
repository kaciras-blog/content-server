package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.RedisOperationsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

final class NoticeConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withAllowBeanDefinitionOverriding(true)
			.withBean(RedisOperationsBuilder.class, () -> new RedisOperationsBuilder(mock(RedisConnectionFactory.class), new ObjectMapper()))
			.withBean(JavaMailSender.class, () -> mock(JavaMailSender.class))
			.withBean(Clock.class, Clock::systemUTC)
			.withUserConfiguration(TestConfiguration.class)
			.withUserConfiguration(JacksonAutoConfiguration.class)
			.withUserConfiguration(NoticeConfiguration.class);

	@ComponentScan("com.kaciras.blog.api.notice")
	static final class TestConfiguration {}

	@Test
	void defaults() {
		runner.run(context -> {
			assertThat(context).doesNotHaveBean(MailService.class);
			assertThat(context).hasSingleBean(NoticeService.class);
		});
	}

	@Test
	void mailService() {
		runner.withPropertyValues("app.notice.mail.from=alice@example.com").run(context -> {
			var noticeService = context.getBean(NoticeService.class);
			var mailService = context.getBean(MailService.class);
			assertThat(noticeService).hasFieldOrPropertyWithValue("mailService", mailService);
		});
	}
}
