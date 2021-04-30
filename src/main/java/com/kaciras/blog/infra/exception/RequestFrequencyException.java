package com.kaciras.blog.infra.exception;

public final class RequestFrequencyException extends HttpStatusException {

	public RequestFrequencyException() { this("操作频率过高，请稍后再试"); }

	public RequestFrequencyException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 429;
	}
}
