package com.kaciras.blog.infra.exception;

/**
 * 表示由用户的输入而产生的异常，对应 HTTP 4xx 状态码，
 * 在控制器的处理过程中抛出的异常将视为正常流程。
 *
 * @see com.kaciras.blog.infra.ExceptionResolver
 */
public abstract class WebBusinessException extends RuntimeException {

	/**
	 * 获取该异常对应的HTTP状态码。
	 * <p>
	 * 没有使用 ResponseStatus 注解，因为使用它的 ResponseStatusExceptionResolver
	 * 依赖于 Servlet，用 ControllerAdvice 更好。
	 *
	 * @return HTTP状态码
	 */
	public abstract int statusCode();

	WebBusinessException(String message) {
		super(message);
	}
}
