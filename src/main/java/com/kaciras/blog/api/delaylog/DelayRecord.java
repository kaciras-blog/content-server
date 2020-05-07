package com.kaciras.blog.api.delaylog;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

@Getter
@Setter
public final class DelayRecord {

	private InetAddress ip;
	private String path;
	private String params;
	private int statusCode;
	private int length;
	private long delay;
	private Instant time;
}
