package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class RateLimiterFilter extends HttpFilter {

	private static final String WAIT_HEADER = "X-RateLimit-Wait";

	private final RedisRateLimiter rateLimiter;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		var key = RedisKeys.RateLimit.of(request.getRemoteHost());
		var waitTime = rateLimiter.acquire(key, 1);
		if (waitTime > 0) {
			response.setStatus(429);
			response.setHeader(WAIT_HEADER, Long.toString(waitTime));
		} else {
			chain.doFilter(request, response);
		}
	}
}
