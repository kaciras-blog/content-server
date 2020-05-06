package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.infra.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

@RequiredArgsConstructor
public final class GenericRateChecker implements RateLimiterChecker {

	private final RateLimiter rateLimiter;

	public long check(InetAddress address, HttpServletRequest request) {
		return rateLimiter.acquire(address.toString(), 1);
	}
}
