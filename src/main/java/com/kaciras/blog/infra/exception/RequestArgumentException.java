package com.kaciras.blog.infra.exception;

public class RequestArgumentException extends WebBusinessException {

	public RequestArgumentException() {
		this("请求中含有不合法的数据");
	}

	public RequestArgumentException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 400;
	}
}
