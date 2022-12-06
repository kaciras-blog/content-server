package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class ResourceDeletedException extends ErrorResponseException {

	public ResourceDeletedException() {
		this(null);
	}

	public ResourceDeletedException(String message) {
		super(HttpStatus.GONE);
		super.getBody().setDetail(message);
		super.getBody().setTitle("请求的资源已经被删除");
	}
}
