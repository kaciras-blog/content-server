package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends HttpFilter {

	private static final String RATE_LIMIT_HEADER = "X-RateLimit-Wait";

	private final RedisRateLimiter rateLimiter;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		var ip = getRemoteAddress(request);

		if (ip != null) {
			var waitTime = rateLimiter.acquire(RedisKeys.RateLimit.of(ip), 1);
			if (waitTime > 0) {
				response.setStatus(429);
				response.setHeader(RATE_LIMIT_HEADER, Long.toString(waitTime));
				response.getWriter().write("操作太快，请歇会再试");
				return;
			}
		}
		chain.doFilter(request, response);
	}

	@Nullable
	private String getRemoteAddress(HttpServletRequest request) {
		if ("OPTIONS".equals(request.getMethod())) {
			return null; // OPTIONS 请求不消耗资源
		}

		var addr = Utils.AddressFromRequest(request);

		// 服务端渲染或反向代理，需要拿到真实IP
		if (addr.isLoopbackAddress() || addr.isSiteLocalAddress()) {
			var forward = request.getHeader("X-Forwarded-For");
			if (forward == null) {
				return null; // 本地且不带转发头，可能是不需要限流的请求
			}
			try {
				return InetAddress.getByName(forward).toString();
			} catch (UnknownHostException e) {
				logger.warn("X-Forwarded-For 的值无效：" + forward);
				return null; // 其他服务器的有错误才会发送无效的头，保守起见不限流
			}
		}
		return addr.toString();
	}
}
