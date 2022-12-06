package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class PermissionException extends ErrorResponseException {

	public PermissionException() {
		super(HttpStatus.FORBIDDEN);
		super.getBody().setTitle("你没有权限执行这个操作");
	}

	public PermissionException(String message) {
		this();
		super.getBody().setDetail(message);
	}
}
