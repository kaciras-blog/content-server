package net.kaciras.blog.api.ratelimit;

import net.kaciras.blog.infra.ratelimit.RateLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class GenericRateCheckerTest {

	private final RateLimiter limiter = mock(RateLimiter.class);
	private final Filter filter = new RateLimitFilter(List.of(new GenericRateChecker(limiter)));

	private MockFilterChain chain = new MockFilterChain();

	@Test
	void validRequest() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(0L);

		var request = new MockHttpServletRequest();
		request.setRemoteAddr("55.66.77.88");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}

	@Test
	void limited() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(555L);

		var request = new MockHttpServletRequest();
		request.setRemoteAddr("55.66.77.88");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		Assertions.assertNull(chain.getRequest());
		Assertions.assertEquals(429, response.getStatus());
		Assertions.assertEquals("555", response.getHeader("X-RateLimit-Wait"));
	}

	@Test
	void localAddr() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(555L);

		var request = new MockHttpServletRequest();
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}

	@Test
	void forward() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(0L);
		when(limiter.acquire(contains("200.0.0.0"), anyInt())).thenReturn(555L);

		var request = new MockHttpServletRequest();
		var response = new MockHttpServletResponse();
		request.setRemoteAddr("192.168.0.50");
		request.addHeader("X-Forwarded-For", "200.0.0.0");

		filter.doFilter(request, response, chain);

		Assertions.assertNull(chain.getRequest());
	}

	@Test
	void localWithoutForward() throws Exception {
		when(limiter.acquire(any(), anyInt())).thenReturn(555L);

		var request = new MockHttpServletRequest();
		var response = new MockHttpServletResponse();
		request.setRemoteAddr("192.168.0.50");

		filter.doFilter(request, response, chain);

		Assertions.assertEquals(request, chain.getRequest());
	}
}
