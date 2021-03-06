package com.kaciras.blog.infra.exception;

public final class ResourceDeletedException extends HttpStatusException {

	public ResourceDeletedException() {
		this("请求的资源已经被删除");
	}

	public ResourceDeletedException(String message) { super(message);}

	@Override
	public int statusCode() {
		return 410;
	}
}
