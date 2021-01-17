package com.kaciras.blog.infra.exception;

public class LegallyProhibitedException extends WebBusinessException {

	public LegallyProhibitedException() {
		this("请求的操作被和谐了");
	}

	public LegallyProhibitedException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 451;
	}
}
