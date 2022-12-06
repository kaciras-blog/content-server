package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class RequestArgumentException extends ResponseStatusException {

	public RequestArgumentException() {
		super(HttpStatus.BAD_REQUEST);
	}

	public RequestArgumentException(String detail) {
		super(HttpStatus.BAD_REQUEST, detail, null, null, null);
	}
}
