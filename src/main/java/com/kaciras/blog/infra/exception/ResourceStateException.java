package com.kaciras.blog.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 在当前资源集合的状态下，无法完成请求的操作。
 */
public final class ResourceStateException extends ResponseStatusException {

	public ResourceStateException() {
		super(HttpStatus.CONFLICT);
	}

	public ResourceStateException(String detail) {
		super(HttpStatus.CONFLICT, detail, null, null, null);
	}
}
