package com.kaciras.blog.infra;

import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 工具类，包含与 HTTP 请求对象相关的静态方法。
 */
public final class RequestUtils {

	private RequestUtils() {}

	/**
	 * 判断一个请求对象是否是不改变状态的安全请求。安全请求的定义见
	 * <a href="https://tools.ietf.org/html/rfc7231#section-4.2.1">RFC7231</a>
	 * <p>
	 * 这里去掉了 TRACE 方法，因为用不到而且还有些安全隐患。
	 *
	 * <h2>模式匹配</h2>
	 * 我觉得这里用传统的 switch 比 pattern match 可读性更好。
	 *
	 * @param request 请求对象
	 * @return 如果是安全请求则为true，否则false
	 */
	@SuppressWarnings("EnhancedSwitchMigration")
	public static boolean isSafeRequest(HttpServletRequest request) {
		switch (request.getMethod()) {
			case "HEAD":
			case "GET":
			case "OPTIONS":
				return true;
			default:
				return false;
		}
	}

	/**
	 * 获取HttpServletRequest的远程地址，处理烦人的 UnknownHostException。
	 *
	 * @param request 请求
	 * @return IP 地址，不会为 null
	 */
	@NonNull
	public static InetAddress addressFrom(HttpServletRequest request) {
		var address = request.getRemoteAddr();
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static boolean isLocalNetwork(InetAddress address) {
		return address.isLoopbackAddress() || address.isSiteLocalAddress();
	}
}
