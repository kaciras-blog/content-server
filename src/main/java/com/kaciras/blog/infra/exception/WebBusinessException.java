package com.kaciras.blog.infra.exception;

/**
 * 表示由用户的输入而产生的异常，这些异常通常需要返回信息给前端。
 */
public abstract class WebBusinessException extends RuntimeException {

	/**
	 * 获取该异常对应的HTTP状态码。
	 * <p>
	 * 【说明】没有使用 ResponseStatus 注解，因为使用它的 ResponseStatusExceptionResolver
	 * 依赖于 Servlet 技术栈，而本项目要兼容 Webflux。
	 *
	 * @return HTTP状态码
	 */
	public abstract int statusCode();

	WebBusinessException(String message) {
		super(message);
	}
}
