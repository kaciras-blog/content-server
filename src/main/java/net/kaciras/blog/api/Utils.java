package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.exception.RequestArgumentException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Utils {

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static InetAddress AddressFromRequest(HttpServletRequest request) {
		var addr = request.getRemoteAddr();

		// 没有地址说明是Mock来的请求
		if (addr == null) {
			return InetAddress.getLoopbackAddress();
		}

		// 这里认为 HttpServletRequest.getRemoteAddr() 获取的格式一定是对的
		try {
			return InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new AssertionError("HttpServletRequest竟然返回无效的地址");
		}
	}

	private Utils() {
	}
}
