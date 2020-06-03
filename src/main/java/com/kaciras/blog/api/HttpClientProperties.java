package com.kaciras.blog.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("app.http-client")
public final class HttpClientProperties {

	public final String proxy;
	public final String executor;

	HttpClientProperties(String proxy, String executor) {
		this.proxy = proxy;
		this.executor = executor;
	}
}
