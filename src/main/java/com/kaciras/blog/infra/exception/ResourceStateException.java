package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/**
 * 在当前资源集合的状态下，无法完成请求的操作。
 */
public final class ResourceStateException extends ErrorResponseException {

	public ResourceStateException() {
		this("");
	}

	public ResourceStateException(String message) {
		super(HttpStatus.CONFLICT);
		super.getBody().setDetail(message);
		super.getBody().setTitle("资源的状态不允许执行该操作");
	}
}
