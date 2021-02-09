package com.kaciras.blog.infra;

import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class RequestUtils {

	private RequestUtils() {}

	/**
	 * 判断一个请求对象是否是不改变状态的安全请求。安全请求的定义见：
	 * https://tools.ietf.org/html/rfc7231#section-4.2.1
	 * <p>
	 * 这里去掉了 TRACE 方法，因为我用不到它，而且它的功能还有些安全隐患。
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
	public static InetAddress addressFromRequest(HttpServletRequest request) {
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
