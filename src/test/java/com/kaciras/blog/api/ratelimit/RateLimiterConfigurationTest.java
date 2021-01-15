package com.kaciras.blog.api.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

final class RateLimiterConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withBean(Clock.class, Clock::systemUTC)
			.withUserConfiguration(RedisAutoConfiguration.class)
			.withUserConfiguration(RateLimiterConfiguration.class);

	@Test
	void noLimitByDefault() {
		runner.run(context -> {
			assertThat(context).doesNotHaveBean(RateLimitFilter.class);
			assertThat(context).doesNotHaveBean(RateLimiterChecker.class);
		});
	}

	@Test
	void generic() {
		runner.withPropertyValues(
				"app.rate-limit.generic.rate=2",
				"app.rate-limit.generic.size=20"
		).run(context -> {
			assertThat(context).hasSingleBean(RateLimitFilter.class);
			assertThat(context).hasBean("genericRateChecker");
		});
	}

	@Test
	void effective() {
		runner.withPropertyValues(
				"app.rate-limit.effective.block-times=1s",
				"app.rate-limit.effective.limits[0].permits=6",
				"app.rate-limit.effective.limits[0].time=30s"
		).run(context -> {
			assertThat(context).hasSingleBean(RateLimitFilter.class);
			assertThat(context).hasBean("effectRateChecker");
		});
	}

	@Test
	void checkerOrder() {
		runner.withPropertyValues(
				"app.rate-limit.effective.block-times=1s",
				"app.rate-limit.effective.limits[0].permits=6",
				"app.rate-limit.effective.limits[0].time=30s",
				"app.rate-limit.generic.rate=2",
				"app.rate-limit.generic.size=2"
		).run(context -> {
			var checkers = context.getBeanFactory().getBeansOfType(RateLimiterChecker.class).values();
			assertThat(checkers).hasSize(2);
			assertThat(checkers).element(1).isInstanceOf(EffectRateChecker.class);
		});
	}
}
