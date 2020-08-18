package com.kaciras.blog.api.accesslog;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.net.InetAddress;
import java.time.Instant;

@Getter
@Setter
public final class AccessRecord {

	private InetAddress ip;

	/** 怎么还有Method为null的 */
	@Nullable
	private String method;

	private String path;

	private int statusCode;

	@Nullable
	private String params;

	@Nullable
	private String userAgent;

	/** 请求体的长度（Content-Length），没有请求体则为null */
	@Nullable
	private Integer length;

	/** 收到请求的时刻 */
	private Instant time;

	/** 处理该请求的用时（毫秒） */
	private long delay;
}
