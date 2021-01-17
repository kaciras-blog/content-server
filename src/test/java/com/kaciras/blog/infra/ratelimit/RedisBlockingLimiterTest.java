package com.kaciras.blog.infra.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestRedisConfiguration.class)
final class RedisBlockingLimiterTest {

	private static final String KEY = "TEST";
	private static final String NAMESPACE = "RATE_LIMIT:";
	private static final int DEFAULT_BLOCK_TIME = 60;

	private final Clock clock = mock(Clock.class);

	@Autowired
	private RedisConnectionFactory connectionFactory;

	@Autowired
	private RedisTemplate<String, Object> template;

	@MockBean
	private RateLimiter inner;

	private RedisBlockingLimiter limiter;

	@BeforeEach
	void setUp() {
		template.unlink(NAMESPACE + KEY);
		when(clock.instant()).thenReturn(Instant.EPOCH);
		limiter = new RedisBlockingLimiter(NAMESPACE, inner, connectionFactory, clock);
		limiter.setBlockTimes(List.of(Duration.ofSeconds(DEFAULT_BLOCK_TIME)));
	}

	@Test
	void delegateToInner() {
		when(inner.acquire(any(), anyInt())).thenReturn(0L);

		var waitTime = limiter.acquire(KEY, 123);

		assertThat(waitTime).isZero();
		verify(inner).acquire(KEY, 123);
	}

	@Test
	void acquireConsequent() {
		when(inner.acquire(any(), anyInt())).thenReturn(0L);

		limiter.acquire(KEY, 123);
		limiter.acquire(KEY, 456);
		var waitTime = limiter.acquire(KEY, 789);

		assertThat(waitTime).isZero();
		verify(inner, times(3)).acquire(any(), anyInt());
	}

	@Test
	void acquireFailed() {
		when(inner.acquire(any(), anyInt())).thenReturn(100L);

		var waitTime = limiter.acquire(KEY, 1);
		assertThat(waitTime).isEqualTo(DEFAULT_BLOCK_TIME);

		when(clock.instant()).thenReturn(Instant.ofEpochSecond(20));
		waitTime = limiter.acquire(KEY, 1);
		assertThat(waitTime).isEqualTo(DEFAULT_BLOCK_TIME - 20);

		verify(inner, times(1)).acquire(any(), anyInt());
	}

	@Test
	void refreshOnReject() {
		limiter.setRefreshOnReject(true);
		when(inner.acquire(any(), anyInt())).thenReturn(100L);
		limiter.acquire(KEY, 1);

		when(clock.instant()).thenReturn(Instant.ofEpochSecond(20));
		var waitTime = limiter.acquire(KEY, 1);

		assertThat(waitTime).isEqualTo(DEFAULT_BLOCK_TIME);
	}

	@Test
	void blockTimeIncrement() {
		limiter.setBlockTimes(List.of(Duration.ofSeconds(10), Duration.ofSeconds(20)));
		when(inner.acquire(any(), anyInt())).thenReturn(100L);

		limiter.acquire(KEY, 1);
		when(clock.instant()).thenReturn(Instant.ofEpochSecond(11));

		var waitTime = limiter.acquire(KEY, 1);
		assertThat(waitTime).isEqualTo(20);
	}

	// 边界情况

	@Test
	void setBlockTimes() {
		assertThatThrownBy(() -> limiter.setBlockTimes(null)).isInstanceOf(NullPointerException.class);

		var invalid1 = List.of(Duration.ofSeconds(-5));
		assertThatThrownBy(() -> limiter.setBlockTimes(invalid1)).isInstanceOf(IllegalArgumentException.class);

		var invalid2 = List.of(Duration.ofSeconds(30), Duration.ofSeconds(10));
		assertThatThrownBy(() -> limiter.setBlockTimes(invalid2)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void noBlockTimes() {
		limiter.setBlockTimes(Collections.emptyList());
		when(inner.acquire(any(), anyInt())).thenReturn(2233L);

		assertThat(limiter.acquire(KEY, 1)).isEqualTo(2233);
		assertThat(limiter.acquire(KEY, 1)).isEqualTo(2233);
	}

	@Test
	void innerReturnsNegative() {
		when(inner.acquire(any(), anyInt())).thenReturn(-1L);

		var waitTime = limiter.acquire(KEY, 1);

		assertThat(waitTime).isNegative();
		when(inner.acquire(any(), anyInt())).thenReturn(0L);
		assertThat(limiter.acquire(KEY, 1)).isZero();
	}
}
