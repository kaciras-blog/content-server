package com.kaciras.blog.infra.exception;

public class DataTooBigException extends WebBusinessException {

	public DataTooBigException() { this("请求所带的数据过长"); }

	public DataTooBigException(String message) {
		super(message);
	}

	@Override
	public int statusCode() {
		return 413;
	}
}
