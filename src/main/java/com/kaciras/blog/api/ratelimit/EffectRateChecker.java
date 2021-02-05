package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * Effect 指有副作用的请求，如提交评论等
 */
@RequiredArgsConstructor
public final class EffectRateChecker implements RateLimitChecker {

	private final RateLimiter rateLimiter;

	@Override
	public long check(InetAddress ip, HttpServletRequest request) {
		if (Misc.isSafeRequest(request)) {
			return 0;
		}
		return rateLimiter.acquire(ip.toString(), 1);
	}
}
