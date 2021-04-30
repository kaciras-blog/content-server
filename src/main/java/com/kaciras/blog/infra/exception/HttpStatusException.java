package com.kaciras.blog.infra.exception;

/**
 * 表示由用户的输入而产生的异常，对应 HTTP 4xx 状态码，
 * 在控制器的处理过程中抛出的异常将视为正常流程。
 *
 * <h2>类似功能</h2>
 * ResponseStatusException 可以替代本类，但它是 WebFlux 技术栈的跟项目不合，怕以后会有不兼容更改。
 * HttpStatusCodeException 是客户端的，也感觉不适合这里。
 *
 * @see com.kaciras.blog.infra.ExceptionResolver
 */
public abstract class HttpStatusException extends RuntimeException {

	/**
	 * 获取该异常对应的HTTP状态码。
	 * <p>
	 * 没有使用 ResponseStatus 注解，因为使用它的 ResponseStatusExceptionResolver
	 * 依赖于 Servlet，用 ControllerAdvice 更好。
	 *
	 * @return HTTP状态码
	 */
	public abstract int statusCode();

	HttpStatusException(String message) { super(message); }
}
