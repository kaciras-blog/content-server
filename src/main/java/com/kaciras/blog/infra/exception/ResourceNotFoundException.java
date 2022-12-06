package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ResourceNotFoundException extends ResponseStatusException {

	public ResourceNotFoundException() {
		super(HttpStatus.NOT_FOUND);
	}

	public ResourceNotFoundException(String detail) {
		super(HttpStatus.NOT_FOUND, detail, null, null, null);
	}
}
