package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class RequestArgumentException extends ErrorResponseException {

	public RequestArgumentException() {
		this(null);
	}

	public RequestArgumentException(String message) {
		super(HttpStatus.BAD_REQUEST);
		super.getBody().setDetail(message);
		super.getBody().setTitle("请求参数错误");
	}
}
