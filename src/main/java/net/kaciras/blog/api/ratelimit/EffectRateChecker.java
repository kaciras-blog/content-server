package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.infrastructure.Misc;
import net.kaciras.blog.infrastructure.ratelimit.RateLimiter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Effect 指有副作用的请求，如提交评论等
 */
@Setter
@RequiredArgsConstructor
@Slf4j
public final class EffectRateChecker implements RateLimiterChecker {

	private static final byte[] REJECT_MSG = "{\"message\":\"请求频率太快，IP被封禁\"}".getBytes(StandardCharsets.UTF_8);

	private final RateLimiter rateLimiter;

	private Pattern whiteList;

	@Override
	public boolean check(InetAddress ip, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Misc.isSafeRequest(request)) {
			return true;
		}
		if (whiteList != null && whiteList.matcher(request.getPathInfo()).find()) {
			return true;
		}
		if (rateLimiter.acquire(ip.toString(), 1) == 0) {
			return true;
		}
		response.setStatus(429);
		response.setContentType("application/json;charset=UTF-8");
		response.setContentLength(REJECT_MSG.length);
		response.getOutputStream().write(REJECT_MSG);
		return false;
	}
}
