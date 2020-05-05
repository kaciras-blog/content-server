package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infra.ratelimit.RateLimiter;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

@RequiredArgsConstructor
public final class GenericRateChecker implements RateLimiterChecker {

	private final RateLimiter rateLimiter;

	public long check(InetAddress address, HttpServletRequest request) {
		return rateLimiter.acquire(address.toString(), 1);
	}
}
