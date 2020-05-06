package com.kaciras.blog.api.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.ratelimit.RateLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class EffectRateCheckerTest {

	private final RateLimiter limiter = mock(RateLimiter.class);
	private final EffectRateChecker checker = new EffectRateChecker(limiter);
	private final Filter filter = new RateLimitFilter(List.of(checker));

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private final MockHttpServletResponse response = new MockHttpServletResponse();
	private final MockFilterChain chain = new MockFilterChain();

	// 默认跳过本地的请求，需要设置一下请求的地址
	@BeforeEach
	void setUp() {
		request.setRemoteAddr("56.152.33.44");
	}

	@Test
	void skipSafeRequest() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(3333L);
		request.setMethod("GET");

		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}

	@Test
	void skipPattern() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(3333L);
		request.setMethod("POST");
		request.setRequestURI("/foo/bar");

		checker.setWhiteList(Pattern.compile("^/fo"));
		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}

	@Test
	void success() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(0L);
		request.setMethod("POST");

		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}

	@Test
	void reject() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(3333L);
		request.setMethod("POST");

		filter.doFilter(request, response, chain);
		var body = response.getContentAsByteArray();
		var bodyJson = objectMapper.readTree(body);

		Assertions.assertNull(chain.getRequest());
		Assertions.assertEquals(response.getContentLength(), body.length);
		Assertions.assertNotNull(bodyJson.get("message"));
	}
}
