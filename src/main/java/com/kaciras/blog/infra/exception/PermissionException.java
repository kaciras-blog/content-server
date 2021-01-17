package com.kaciras.blog.infra.exception;

public class PermissionException extends WebBusinessException {

	public PermissionException() { this("你没有权限执行这个操作"); }

	public PermissionException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 403;
	}
}
