package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class ResourceNotFoundException extends ErrorResponseException {

	public ResourceNotFoundException() {
		this("");
	}

	public ResourceNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND);
		super.getBody().setDetail(message);
		super.getBody().setTitle("找不到所请求的资源");
	}
}
