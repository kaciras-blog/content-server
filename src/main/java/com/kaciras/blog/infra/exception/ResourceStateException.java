package com.kaciras.blog.infra.exception;

/**
 * 在当前资源集合的状态下，无法完成请求的操作。
 */
public class ResourceStateException extends WebBusinessException {

	public ResourceStateException() {
		this("资源的状态不允许执行请求的操作");
	}

	public ResourceStateException(String message) { super(message); }

	@Override
	public int statusCode() {
		return 409;
	}
}
