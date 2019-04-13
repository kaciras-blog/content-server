package net.kaciras.blog.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;

@ActiveProfiles("test")
@SpringBootTest(classes = RateLimiterConfiguration.class)
final class RedisRateLimiterTest {

	private static final String KEY = "RATE_LIMITER_TEST";

	@Autowired
	private RedisTemplate<String, Object> template;

	private Clock clock = Mockito.mock(Clock.class);

	private RedisRateLimiter createLimiter(double rate, int size) {
		var limiter = new RedisRateLimiter(clock, template);
		limiter.setRate(rate);
		limiter.setBucketSize(size);
		limiter.setCacheTime(10);
		return limiter;
	}

	@AfterEach
	void clean() {
		template.unlink(KEY);
		Mockito.reset(clock);
	}

	@Test
	void testAcquire() {
		var limiter = createLimiter(2, 100);
		Mockito.when(clock.instant()).thenReturn(Instant.ofEpochSecond(0));

		Assertions.assertThat(limiter.acquire(KEY, 50)).isZero();
		Assertions.assertThat(limiter.acquire(KEY, 40)).isZero();
		Assertions.assertThat(limiter.acquire(KEY, 40)).isEqualTo(15);
	}

	@Test
	void testRestore() {
		var limiter = createLimiter(2, 100);
		Mockito.when(clock.instant()).thenReturn(Instant.ofEpochSecond(0));
		Assertions.assertThat(limiter.acquire(KEY, 100)).isZero();

		Mockito.when(clock.instant()).thenReturn(Instant.ofEpochSecond(50));
		Assertions.assertThat(limiter.acquire(KEY, 100)).isZero();
		Assertions.assertThat(limiter.acquire(KEY, 30)).isEqualTo(15);
	}

	/**
	 * 速率为0是一个边界条件，需要测试下。
	 * 另外，速率小于0的情况我没想出有什么用途，就不测了。
	 */
	@Test
	void testZeroRate() {
		var limiter = createLimiter(0, 200);
		Mockito.when(clock.instant()).thenReturn(Instant.ofEpochSecond(0));

		Assertions.assertThat(limiter.acquire(KEY, 200)).isZero();
		Assertions.assertThat(limiter.acquire(KEY, 3)).isEqualTo(Long.MIN_VALUE);
	}

}
