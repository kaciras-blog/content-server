package com.kaciras.blog.api.delaylog;

import com.kaciras.blog.api.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * 记录访问用时的过滤器，可以用来检测用时过长的请求。
 *
 * TODO: 话说为什么要记到数据库，用日志不好么……
 */
@Order(-10)
@RequiredArgsConstructor
@Component
public final class DelayLoggingFilter extends HttpFilter {

	/** 65秒可以视为响应超时 */
	private static final int MAX_DELAY = 65535;

	private final Clock clock;

	// 跟定时任务共用一个线程池，就不再额外开线程了
	private final ThreadPoolTaskScheduler threadPool;

	private final DelayLoggingDAO delayLoggingDAO;

	@Value("${kaciras.delay-log.threshold}")
	private Duration threshold;

	@Override
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain) throws IOException, ServletException {
		var instant = clock.instant();
		try {
			chain.doFilter(request, response);
		} finally {
			threadPool.execute(() -> log(request, response, instant));
		}
	}

	// 垃圾@Async对内部调用不代理，即使设置了 proxyTargetClass 也没用
	private void log(HttpServletRequest request, HttpServletResponse response, Instant start) {
		var path = request.getRequestURI();
		if (path == null) return; // 忽略一些奇葩情况

		var delay = Duration.between(start, clock.instant());
		if (delay.compareTo(threshold) < 0) return;

		var record = new DelayRecord();
		record.setIp(Utils.addressFromRequest(request));
		record.setPath(path);
		record.setParams(request.getQueryString());
		record.setStatusCode(response.getStatus());
		record.setTime(start);
		record.setLength(request.getContentLength());
		record.setDelay(Math.min(delay.toMillis(), MAX_DELAY));

		delayLoggingDAO.insert(record);
	}
}
