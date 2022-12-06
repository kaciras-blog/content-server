package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class LegallyProhibitedException extends ResponseStatusException {

	public LegallyProhibitedException() {
		super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
	}

	public LegallyProhibitedException(String detail) {
		super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, detail, null, null, null);
	}
}
