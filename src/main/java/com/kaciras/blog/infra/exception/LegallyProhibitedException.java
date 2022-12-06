package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class LegallyProhibitedException extends ErrorResponseException {

	public LegallyProhibitedException() {
		super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
		super.getBody().setTitle("请求的操作被和谐了");
	}

	public LegallyProhibitedException(String message) {
		this();
		super.getBody().setDetail(message);
	}
}
