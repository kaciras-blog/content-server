package com.kaciras.blog.api.ratelimit;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

@FunctionalInterface
public interface RateLimitChecker {

	/**
	 * 检查请求是否过于频繁，如果是则返回等待的时间，否则返回零。
	 *
	 * <h2>负数的对待方式</h2>
	 * 负数的等待时间没有意义，如果是其它情况禁止的话请用 Filter 而不是该接口。
	 * 所以不要返回负数，如果返回则后续的处理是未定义的。
	 *
	 * @param address 访问者的地址
	 * @param request 请求
	 * @return 等待的时间，零表示无需等待正常通过
	 */
	long check(InetAddress address, HttpServletRequest request);
}
