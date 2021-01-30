package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.RedisOperationsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

final class FriendConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withAllowBeanDefinitionOverriding(true)
			.withBean(RedisOperationsBuilder.class, () -> new RedisOperationsBuilder(null, new ObjectMapper()))
			.withBean(TaskScheduler.class, () -> mock(TaskScheduler.class))
			.withBean(FriendValidateService.class, () -> mock(FriendValidateService.class))
			.withUserConfiguration(FriendConfiguration.class);

	@Test
	void scheduleValidator() {
		runner.withPropertyValues("app.validate-friend=true").run(ctx -> {
			verify(ctx.getBean(TaskScheduler.class)).scheduleAtFixedRate(any(), eq(Duration.ofDays(1)));
		});
	}
}
