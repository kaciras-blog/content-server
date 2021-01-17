package com.kaciras.blog.infra;

import com.kaciras.blog.infra.exception.WebBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Set;

/**
 * 处理 WebBusinessException 异常和MVC参数绑定时的异常。
 * <p>
 * 【更新】原先这个类放在 KxWebUtilsAutoConfiguration 内部，但在测试里
 * 它被直接当作 bean 给创建而不是走  exceptionResolver() 方法，故移出来。
 */
@RequiredArgsConstructor
@ControllerAdvice
@ResponseBody
public final class ExceptionResolver {

	static final String DEFAULT_MESSAGE = "请求参数或内容不合法";

	private static final Set<Class<?>> ARGUMENT_EXCEPTIONS = Set.of(
			MethodArgumentNotValidException.class,
			BindException.class,
			MethodArgumentTypeMismatchException.class
	);

	private final boolean debug;

	@ExceptionHandler
	public ResponseEntity<Object> handle(Exception ex) throws Exception {
		/* 自己定义的异常 */
		if (ex instanceof WebBusinessException) {
			return ResponseEntity
					.status(((WebBusinessException) ex).statusCode())
					.body(Map.of("message", ex.getMessage()));
		}

		/* 控制器方法的参数绑定失败 */
		if (ARGUMENT_EXCEPTIONS.contains(ex.getClass())) {
			var message = debug ? debugMessage(ex) : DEFAULT_MESSAGE;
			return ResponseEntity.status(400).body(Map.of("message", message));
		}

		throw ex; // 内部错误，或是MVC的异常就按默认处理
	}

	private String debugMessage(Exception e) {
		if (e instanceof MethodArgumentNotValidException) {
			var ex = (MethodArgumentNotValidException) e;
			return ex.getMessage();
		}
		if (e instanceof MethodArgumentTypeMismatchException) {
			var ex = (MethodArgumentTypeMismatchException) e;
			return String.format("%s 的参数：%s 类型错误，需要：%s，实际：%s",
					ex.getParameter().getMethod(), ex.getName(), ex.getRequiredType(), ex.getValue());
		}
		return DEFAULT_MESSAGE;
	}
}
