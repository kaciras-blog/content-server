package com.kaciras.blog.api.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class RateLimitFilterTest {

	private final MockHttpServletResponse response = new MockHttpServletResponse();

	private boolean accepted;

	void doFilter(Filter filter, HttpServletRequest request) throws Exception {
		filter.doFilter(request, response, (a, b) -> {
			accepted = true;
			response.setStatus(200);
		});
	}

	@Test
	void empty() throws Exception {
		var filter = new RateLimitFilter(List.of());
		var request = new MockHttpServletRequest();
		request.setRemoteAddr("1234:6666::8888");

		doFilter(filter, request);
		assertThat(accepted).isTrue();
	}

	@Test
	void allowLAN() throws Exception {
		var checker = mock(RateLimitChecker.class);
		when(checker.check(any(), any())).thenReturn(100L);
		var filter = new RateLimitFilter(List.of(checker));

		var request = new MockHttpServletRequest();
		request.setRemoteAddr("::1");

		doFilter(filter, request);
		assertThat(accepted).isTrue();
	}

	@Test
	void reject() throws Exception {
		var checker = mock(RateLimitChecker.class);
		when(checker.check(any(), any())).thenReturn(100L);
		var filter = new RateLimitFilter(List.of(checker));

		var request = new MockHttpServletRequest();
		request.setRemoteAddr("1234:6666::8888");

		doFilter(filter, request);

		assertThat(accepted).isFalse();
		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getHeader("X-RateLimit-Wait")).isEqualTo("100");
	}

	@Test
	void allowed() throws Exception {
		var checker = mock(RateLimitChecker.class);
		when(checker.check(any(), any())).thenReturn(0L);
		var filter = new RateLimitFilter(List.of(checker));

		var request = new MockHttpServletRequest();
		request.setRemoteAddr("1234:6666::8888");

		doFilter(filter, request);
		assertThat(accepted).isTrue();
	}
}
