package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.infra.Misc;
import net.kaciras.blog.infra.ratelimit.RateLimiter;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * Effect 指有副作用的请求，如提交评论等
 */
@Setter
@RequiredArgsConstructor
public final class EffectRateChecker implements RateLimiterChecker {

	private final RateLimiter rateLimiter;

	private Pattern whiteList;

	@Override
	public long check(InetAddress ip, HttpServletRequest request) {
		if (Misc.isSafeRequest(request)) {
			return 0;
		}
		if (whiteList != null && whiteList.matcher(request.getRequestURI()).find()) {
			return 0;
		}
		return rateLimiter.acquire(ip.toString(), 1);
	}
}
