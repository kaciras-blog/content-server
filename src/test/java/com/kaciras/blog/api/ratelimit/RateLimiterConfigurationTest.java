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
			assertThat(context).doesNotHaveBean(RateLimitChecker.class);
		});
	}

	@Test
	void generic() {
		runner.withPropertyValues(
				"app.rate-limiter.generic.rate=2",
				"app.rate-limiter.generic.size=20"
		).run(context -> {
			assertThat(context).hasSingleBean(RateLimitFilter.class);
			assertThat(context).hasBean("genericRateChecker");
		});
	}

	@Test
	void effective() {
		runner.withPropertyValues(
				"app.rate-limiter.effective.block-times=1s",
				"app.rate-limiter.effective.limits[0].permits=6",
				"app.rate-limiter.effective.limits[0].time=30s"
		).run(context -> {
			assertThat(context).hasSingleBean(RateLimitFilter.class);
			assertThat(context).hasBean("effectRateChecker");
		});
	}

	@Test
	void checkerOrder() {
		runner.withPropertyValues(
				"app.rate-limiter.effective.block-times=1s",
				"app.rate-limiter.effective.limits[0].permits=6",
				"app.rate-limiter.effective.limits[0].time=30s",
				"app.rate-limiter.generic.rate=2",
				"app.rate-limiter.generic.size=2"
		).run(context -> {
			var checkers = context.getBeanFactory().getBeansOfType(RateLimitChecker.class).values();
			assertThat(checkers).hasSize(2);
			assertThat(checkers).element(1).isInstanceOf(EffectRateChecker.class);
		});
	}
}
