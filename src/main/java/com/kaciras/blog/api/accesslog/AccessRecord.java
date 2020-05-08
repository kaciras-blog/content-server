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

	private String method;
	private String path;
	private int statusCode;
	private String params;

	/** UA可以自定义，可能有些奇葩无法解析，只能直接保存，然后在分析时解析 */
	@Nullable
	private String userAgent;

	private Integer length;

	private long delay;
	private Instant time;
}
