package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.ratelimit.RateLimiter;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;

/*
 * 【注意】关于CORS预检请求的处理：
 *
 * CORS预检请求(OPTIONS 带有 Origin 和 Access-Control-Request-Method)发起的时机和数量无法预测，
 * 但在 CorsFilter 里已经过滤掉了。
 * 于不合规范的 OPTIONS 请求视为非正常行为，一样进行速率限制，故这里不检查请求的方法。
 * 这要求该过滤器在 CorsFilter 之后，请用 Order 来改变顺序。
 *
 * @see org.springframework.web.filter.CorsFilter#doFilterInternal
 */
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE + 21)
public final class GenericRateChecker implements RateLimiterChecker {

	/** 当达到限制时返回一个响应头告诉客户端相关信息 */
	private static final String RATE_LIMIT_HEADER = "X-RateLimit-Wait";

	private final RateLimiter rateLimiter;

	public boolean check(InetAddress address, HttpServletRequest request, HttpServletResponse response) throws IOException {
		var waitTime = rateLimiter.acquire(address.toString(), 1);
		if (waitTime == 0) {
			return true;
		} else if (waitTime < 0) {
			response.setStatus(403);
		} else {
			response.setStatus(429);
			response.setHeader(RATE_LIMIT_HEADER, Long.toString(waitTime));
			response.getWriter().write("操作太快，请歇会再试");
		}
		return false;
	}
}
