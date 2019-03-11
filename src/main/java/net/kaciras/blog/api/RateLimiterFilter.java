package net.kaciras.blog.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;

@Slf4j
@Component
public class RateLimiterFilter extends HttpFilter {

	private final RedisRateLimiter rateLimiter;

	@Autowired
	public RateLimiterFilter(Clock clock, RedisConnectionFactory factory) {
		rateLimiter = new RedisRateLimiter(clock, factory);
		rateLimiter.setRate(1);
		rateLimiter.setBucketSize(5);
		rateLimiter.setCacheTime(60);
	}

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		var waitTime = rateLimiter.acquire(request.getRemoteHost(), 1);
		if (waitTime > 0) {
			response.setStatus(429);
			response.setHeader("X-RateLimit-Wait", Long.toString(waitTime));
		} else {
			chain.doFilter(request, response);
		}
	}
}
