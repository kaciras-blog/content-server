package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class RequestFrequencyException extends ResponseStatusException {

	public RequestFrequencyException() {
		super(HttpStatus.TOO_MANY_REQUESTS);
	}

	public RequestFrequencyException(String detail) {
		super(HttpStatus.TOO_MANY_REQUESTS, detail, null, null, null);
	}
}
