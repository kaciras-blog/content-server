package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PermissionException extends ResponseStatusException {

	public PermissionException() {
		super(HttpStatus.FORBIDDEN);
	}

	public PermissionException(String detail) {
		super(HttpStatus.FORBIDDEN, detail, null, null, null);
	}
}
