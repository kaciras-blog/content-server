package com.kaciras.blog.infra.exception;

public final class PermissionException extends HttpStatusException {

	public PermissionException() { this("你没有权限执行这个操作"); }

	public PermissionException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 403;
	}
}
