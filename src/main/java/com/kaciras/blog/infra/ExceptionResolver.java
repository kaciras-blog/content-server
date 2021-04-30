package com.kaciras.blog.infra;

import com.kaciras.blog.infra.exception.HttpStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * 处理 HTTP 请求过程中出现的异常，返回正确的响应码并打印日志。
 *
 * <h3>更新1</h3>
 * 原先这个类放在 KxWebUtilsAutoConfiguration 内部，
 * 但在测试里它被直接当作 bean 给创建而不是走 exceptionResolver() 方法，故移出来。
 *
 * <h3>更新2</h3>
 * 重构后不再区分调试模式返回的信息，调试信息直接打印到日志，将日志开到 DEBUG 即可看见。
 */
@RestControllerAdvice
@Slf4j
public final class ExceptionResolver {

	static final String DEFAULT_MESSAGE = "请求参数或内容不合法";

	// 直接对应 4xx 响应码的异常
	@ExceptionHandler(HttpStatusException.class)
	public ResponseEntity<?> handle(HttpStatusException e) {
		logger.debug(e.getMessage(), e);
		return ResponseEntity
				.status(e.statusCode())
				.body(Map.of("message", e.getMessage()));
	}

	// 控制器方法的参数绑定或校验失败
	@ExceptionHandler({BindException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<?> handle(Exception e) {
		logger.debug(DEFAULT_MESSAGE, e);
		return ResponseEntity
				.status(400)
				.body(Map.of("message", DEFAULT_MESSAGE));
	}
}
