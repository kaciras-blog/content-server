package com.kaciras.blog.infra.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.http-client")
@RequiredArgsConstructor
public final class HttpClientProperties {

	/**
	 * 连接超时时间，为 null 则不超时
	 */
	public final Duration timeout;

	/**
	 * 代理地址，格式 host:port，为 null 或空字符串则不使用代理
	 */
	public final String proxy;

	/**
	 * HttpClient 使用的线程池的 Bean Name，null 和空字符串不使用。
	 */
	public final String executor;
}
