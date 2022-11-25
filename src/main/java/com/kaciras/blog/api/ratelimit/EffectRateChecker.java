package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.infra.RequestUtils;
import com.kaciras.blog.infra.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;

/**
 * Effect 指有副作用的请求，如提交评论，这类请求使用比安全请求更严格的限制规则。
 */
@RequiredArgsConstructor
final class EffectRateChecker implements RateLimitChecker {

	private final RateLimiter rateLimiter;

	@Override
	public long check(InetAddress ip, HttpServletRequest request) {
		if (RequestUtils.isSafeRequest(request)) {
			return 0;
		}
		return rateLimiter.acquire(ip.toString(), 1);
	}
}
