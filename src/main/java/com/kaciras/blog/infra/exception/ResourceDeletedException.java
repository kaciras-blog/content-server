package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ResourceDeletedException extends ResponseStatusException {

	public ResourceDeletedException() {
		super(HttpStatus.GONE);
	}

	public ResourceDeletedException(String detail) {
		super(HttpStatus.GONE, detail, null, null, null);
	}
}
