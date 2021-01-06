package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.infra.ratelimit.RateLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.InetAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class EffectRateCheckerTest {

	private final RateLimiter limiter = mock(RateLimiter.class);
	private final EffectRateChecker checker = new EffectRateChecker(limiter);

	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private InetAddress address;

	// 默认跳过本地的请求，需要设置一下请求的地址
	@BeforeEach
	void setUp() throws Exception {
		address = InetAddress.getByName("56.152.33.44");
		request.setRemoteAddr("56.152.33.44");
	}

	@Test
	void skipSafeRequest() {
		when(limiter.acquire(any(), anyInt())).thenReturn(333L);
		request.setMethod("GET");

		var waitTime = checker.check(address, request);
		Assertions.assertEquals(0, waitTime);
	}

	@Test
	void check() {
		when(limiter.acquire(any(), anyInt())).thenReturn(333L);
		request.setMethod("POST");

		var waitTime = checker.check(address, request);
		Assertions.assertEquals(333L, waitTime);
	}
}
