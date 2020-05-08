package com.kaciras.blog.api.accesslog;

import com.kaciras.blog.api.Utils;
import lombok.RequiredArgsConstructor;
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
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain) throws IOException, ServletException {
		var instant = clock.instant();
		try {
			chain.doFilter(request, response);
		} finally {
			var end = clock.instant();
			threadPool.execute(() -> log(request, response, instant, end));
		}
	}

	// 垃圾@Async对内部调用不代理，即使设置了 proxyTargetClass 也没用
	private void log(HttpServletRequest request, HttpServletResponse response, Instant start, Instant end) {
		var path = request.getRequestURI();
		if (path == null) return; // 忽略一些奇葩情况

		var record = new AccessRecord();
		record.setIp(Utils.addressFromRequest(request));
		record.setMethod(request.getMethod());
		record.setPath(path);
		record.setStatusCode(response.getStatus());
		record.setTime(start);
		record.setParams(request.getQueryString());

		// 响应超时，再大的数也没有意义，限制到65535。
		var delay = Duration.between(start, end).toMillis();
		record.setDelay(Math.min(delay, MAX_DELAY));

		if (request.getContentLength() > -1) {
			record.setLength(request.getContentLength());
		}

		// 正常的 User-Agent 不会太长，过长的一般也没什么意义，这里直接截断到255字符以内
		Optional.ofNullable(request.getHeader("User-Agent"))
				.map(ua -> ua.substring(0, Math.min(ua.length(), UA_MAX_LENGTH)))
				.ifPresent(record::setUserAgent);

		accessLoggingDAO.insert(record);
	}
}
