package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import java.util.concurrent.Executor;

/**
 * Effect 指有副作用的请求，如提交评论等
 */
@Setter
@RequiredArgsConstructor
@Slf4j
@Order(Integer.MIN_VALUE + 20) // 比CORS过滤器低一点，比其他的高
public class EffectRateLimitFilter extends AbstractRateLimitFilter {

	private static final byte[] REJECT_MSG = "{\"message\":\"请求频率太快，IP被封禁\"}".getBytes(StandardCharsets.UTF_8);
	private static final byte[] EMPTY = new byte[]{0};

	private final RateLimiter rateLimiter;
	private final RedisTemplate<String, byte[]> redisTemplate;
	private final Executor threadPool;

	private Duration banTime = Duration.ofHours(1);
	private boolean refreshOnReject;

	/**
	 * 异步统计请求频率：如果没有被封禁就请求成功，同时在另一个线程中获取限流器的令牌，如果达到限制则封禁，
	 * 本次的请求仍然是成功的，封禁从下一个请求开始才生效。
	 * <p>
	 * 这样做把对限流器的访问异步化，可以减少对请求的阻塞时间（一次Redis访问）而提高性能。（应该能提高一些吧我也没测）
	 */
	@Override
	protected boolean check(InetAddress ip, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Misc.isSafeRequest(request)) {
			return true;
		}
		var blockKey = RedisKeys.EffectBlocking.of(ip);

		if (Utils.nullableBool(redisTemplate.hasKey(blockKey))) {
			reject(response);
			if (refreshOnReject) {
				redisTemplate.opsForValue().set(blockKey, EMPTY, banTime);
			}
			return false;
		}

		threadPool.execute(() -> {
			var waitTime = rateLimiter.acquire(RedisKeys.EffectRate.of(ip), 1);
			if (waitTime > 0) {
				logger.warn("{} 请求过快，被封禁1小时", ip);
				redisTemplate.opsForValue().set(blockKey, EMPTY, banTime);
			}
		});

		return true;
	}

	private void reject(HttpServletResponse response) throws IOException {
		response.setStatus(429);
		response.setContentType("application/json;charset=UTF-8");
		response.setContentLength(REJECT_MSG.length);
		response.getOutputStream().write(REJECT_MSG);
	}
}
