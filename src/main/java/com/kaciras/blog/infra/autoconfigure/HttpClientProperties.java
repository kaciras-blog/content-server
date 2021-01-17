package com.kaciras.blog.infra.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@ConstructorBinding
@ConfigurationProperties("app.http-client")
public final class HttpClientProperties {

	/**
	 * 连接超时时间
	 */
	public final Duration timeout;

	/**
	 * 代理地址，格式 host:port，为null则不使用代理
	 */
	public final String proxy;

	/**
	 * HttpClient使用的线程池的 Bean Name
	 */
	public final String executor;

	HttpClientProperties(Duration timeout, String proxy, String executor) {
		this.timeout = timeout;
		this.proxy = proxy;
		this.executor = executor;
	}
}
