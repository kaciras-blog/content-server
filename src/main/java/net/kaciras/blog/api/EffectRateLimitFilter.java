package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Order(Integer.MIN_VALUE + 20)
public class EffectRateLimitFilter extends HttpFilter {

	private static final byte[] rejectMsg = "请求频率太快，你的IP被封禁1小时".getBytes(StandardCharsets.UTF_8);
	private static final byte[] empty = new byte[]{0};

	private final RedisRateLimiter rateLimiter;
	private final RedisTemplate<String, byte[]> redisTemplate;

	private final Duration banTime = Duration.ofHours(1);

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		var method = request.getMethod();
		if ("GET".equals(method) || "HEAD".equals(method)) {
			chain.doFilter(request, response);
			return;
		}
		var ip = RateLimitFilter.getRemoteAddress(request);
		var blockKey = "eb:" + ip;

		if (Utils.nullableBool(redisTemplate.hasKey(blockKey))) {
			reject(response);
		} else if (rateLimiter.acquire("er:" + ip, 1) > 0) {
			reject(response);
			redisTemplate.opsForValue().set(blockKey, empty, banTime);
			logger.warn("{}请求过快，被封禁1小时", ip);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void reject(HttpServletResponse response) throws IOException {
		response.setStatus(403);
		response.setContentType("text/plain;charset=UTF-8");
		response.setContentLength(rejectMsg.length);
		response.getOutputStream().write(rejectMsg);
	}
}
