package net.kaciras.blog.api.accesslog;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Order(-10)
@RequiredArgsConstructor
@Component
public final class AccessLoggingFilter extends HttpFilter {

	private static final int UA_MAX_LENGTH = 255;
	private static final int MAX_DELAY = 65535;

	private final Clock clock;

	// 跟定时任务共用一个线程池，就不再额外开线程了
	private final ThreadPoolTaskScheduler threadPool;

	private final AccessLoggingDAO accessLoggingDAO;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if ("OPTIONS".equals(request.getMethod())) {
			chain.doFilter(request, response);
			return;
		}
		var instant = clock.instant();
		try {
			chain.doFilter(request, response);
		} finally {
			threadPool.execute(() -> log(request, response, instant));
		}
	}

	// 垃圾@Async对内部调用不代理，即使设置了 proxyTargetClass 也没用
	private void log(HttpServletRequest request, HttpServletResponse response, Instant startInstant) {
		var record = new AccessRecord();
		record.setStartTime(LocalDateTime.ofInstant(startInstant, clock.getZone()));
		record.setIp(Utils.AddressFromRequest(request));
		record.setPath(request.getRequestURI());
		record.setStatusCode(response.getStatus());

		// 正常的 User-Agent 不会太长，过长的一般也没什么意义，这里直接截断到255字符以内
		Optional.ofNullable(request.getHeader("User-Agent"))
				.map(userAgent -> userAgent.substring(0, Math.min(userAgent.length(), UA_MAX_LENGTH)))
				.ifPresent(record::setUserAgent);

		var delay = Duration.between(startInstant, clock.instant()).toMillis();
		record.setDelay(Math.min(delay, MAX_DELAY)); // 响应超时，再大的数也没有意义，限制到65535。

		accessLoggingDAO.insert(record);
	}
}
