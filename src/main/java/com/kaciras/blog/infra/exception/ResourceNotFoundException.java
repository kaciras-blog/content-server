package com.kaciras.blog.infra.exception;

public class ResourceNotFoundException extends WebBusinessException {

	public ResourceNotFoundException() {
		this("找不到所请求的资源");
	}

	public ResourceNotFoundException(String message) { super(message); }

	@Override
	public int statusCode() {
		return 404;
	}
}
