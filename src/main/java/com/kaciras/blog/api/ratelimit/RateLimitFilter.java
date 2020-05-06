package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.api.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
@Order(Integer.MIN_VALUE + 20)
@Slf4j
@RequiredArgsConstructor
public final class RateLimitFilter extends HttpFilter {

	/** 当达到限制时返回一些相关信息 */
	private static final String RATE_LIMIT_HEADER = "X-RateLimit-Wait";
	private static final byte[] REJECT_MSG = "{\"message\":\"请求频率太快，请歇会再来\"}".getBytes(StandardCharsets.UTF_8);

	private final List<RateLimiterChecker> checkers;

	@Override
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain) throws IOException, ServletException {
		var ip = getClientAddress(request);
		if (ip != null) {
			for (var checker : checkers) {
				var waitTime = checker.check(ip, request);
				if (waitTime < 0) {
					response.setStatus(403);
					logger.warn("限流器返回了负值，[waitTime={}, IP={}]", waitTime, ip);
					return;
				} else if (waitTime > 0) {
					response.setStatus(429);
					response.setHeader(RATE_LIMIT_HEADER, Long.toString(waitTime));
					response.setContentLength(REJECT_MSG.length);
					response.getOutputStream().write(REJECT_MSG);
					logger.warn("{}被限流{}秒", ip, waitTime);
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Nullable
	private InetAddress getClientAddress(HttpServletRequest request) {
		var address = Utils.addressFromRequest(request);

		// 服务端渲染或反向代理，需要拿到真实IP
		if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
			var forward = request.getHeader("X-Forwarded-For");
			if (forward == null) {
				return null; // 本地且不带转发头，可能是不需要限流的请求
			}
			try {
				return InetAddress.getByName(forward);
			} catch (UnknownHostException e) {
				logger.warn("无效的 X-Forwarded-For：" + forward);
				return null; // 其他服务器有错误才会发送无效的头，保守起见不限流
			}
		}
		return address;
	}
}
