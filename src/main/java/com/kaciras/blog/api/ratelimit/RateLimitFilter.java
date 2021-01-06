package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.api.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 限流过滤器，拦截过于频繁的访问以保护系统。
 *
 * <h2>关于 CORS 预检请求的处理</h2>
 * CORS 预检请求发起的时机和数量无法预测，但在 CorsFilter 里已经过滤掉了。
 * 这要求该过滤器在 CorsFilter 之后，请用 Order 来改变顺序。
 * 不合规范的 OPTIONS 请求视为非正常行为，一样进行速率限制，故这里不检查请求的方法。
 *
 * @see org.springframework.web.filter.CorsFilter#doFilterInternal
 */
@Order(Integer.MIN_VALUE + 20)
@Slf4j
@RequiredArgsConstructor
public final class RateLimitFilter extends HttpFilter {

	/** 当达到限制时返回一些相关信息 */
	private static final String RATE_LIMIT_HEADER = "X-RateLimit-Wait";

	private final List<RateLimiterChecker> checkers;

	@Override
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain) throws IOException, ServletException {
		var ip = Utils.addressFromRequest(request);
		if (ip.isLoopbackAddress() || ip.isSiteLocalAddress()) {
			chain.doFilter(request, response);
		} else {
			var wait = checkers.stream()
					.map(c -> c.check(ip, request))
					.filter(time -> time != 0)
					.findFirst();

			if (wait.isEmpty()) {
				chain.doFilter(request, response);
			} else {
				var waitTime = wait.get();
				logger.warn("{}被限流{}秒", ip, waitTime);
				response.setStatus(429);
				response.setHeader(RATE_LIMIT_HEADER, Long.toString(waitTime));
			}
		}
	}
}
