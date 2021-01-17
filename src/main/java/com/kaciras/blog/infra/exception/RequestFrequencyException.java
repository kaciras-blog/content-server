package com.kaciras.blog.infra.exception;

public class RequestFrequencyException extends WebBusinessException {

	public RequestFrequencyException() { this("操作频率过高，请稍后再试"); }

	public RequestFrequencyException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 429;
	}
}
