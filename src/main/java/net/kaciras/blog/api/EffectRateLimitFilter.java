package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.infrastructure.Misc;
import net.kaciras.blog.infrastructure.ratelimit.RateLimiter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Effect 指有副作用的请求，如提交评论等
 */
@RequiredArgsConstructor
@Slf4j
@Order(Integer.MIN_VALUE + 20) // 比CORS过滤器低一点，比其他的高
public class EffectRateLimitFilter extends AbstractRateLimitFilter {

	private static final byte[] REJECT_MSG = "请求频率太快，你的IP被封禁1小时".getBytes(StandardCharsets.UTF_8);
	private static final byte[] EMPTY = new byte[]{0};

	private final RateLimiter rateLimiter;
	private final RedisTemplate<String, byte[]> redisTemplate;

	private final Duration banTime = Duration.ofHours(1);

	@Override
	protected boolean check(InetAddress ip, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Misc.isSafeRequest(request)) {
			return true;
		}
		var blockKey = RedisKeys.EffectBlocking.of(ip);

		if (Utils.nullableBool(redisTemplate.hasKey(blockKey))) {
			reject(response);
		} else if (rateLimiter.acquire(RedisKeys.EffectRate.of(ip), 1) > 0) {
			reject(response);
			logger.warn("{} 请求过快，被封禁1小时", ip);
			redisTemplate.opsForValue().set(blockKey, EMPTY, banTime);
		} else {
			return true;
		}
		return false;
	}

	private void reject(HttpServletResponse response) throws IOException {
		response.setStatus(403);
		response.setContentType("text/plain;charset=UTF-8");
		response.setContentLength(REJECT_MSG.length);
		response.getOutputStream().write(REJECT_MSG);
	}
}
