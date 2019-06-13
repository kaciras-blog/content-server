package net.kaciras.blog.api.ratelimit;

import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.api.Utils;
import org.springframework.lang.Nullable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public abstract class AbstractRateLimitFilter extends HttpFilter {

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		var ip = getClientAddress(request);
		if (ip == null || check(ip, request, response)) {
			chain.doFilter(request, response);
		}
	}

	@Nullable
	private static InetAddress getClientAddress(HttpServletRequest request) {
		var address = Utils.AddressFromRequest(request);

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
				return null; // 其他服务器的有错误才会发送无效的头，保守起见不限流
			}
		}
		return address;
	}

	protected abstract boolean check(InetAddress address, HttpServletRequest request,
									 HttpServletResponse response) throws IOException;
}
