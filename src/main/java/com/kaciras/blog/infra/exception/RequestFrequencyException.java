package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public final class RequestFrequencyException extends ErrorResponseException {

	public RequestFrequencyException() { this("操作频率过高，请稍后再试"); }

	public RequestFrequencyException(String message) {
		super(HttpStatus.TOO_MANY_REQUESTS);
		super.getBody().setDetail(message);
		super.getBody().setTitle("操作频率过高，请稍后再试");
	}
}
