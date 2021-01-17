package com.kaciras.blog.infra.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.util.concurrent.Executor;

/**
 * 虽然Spring的RestTemplate也不错，但我还是喜欢原生的HttpClient啦
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HttpClientProperties.class)
@RequiredArgsConstructor
public class HttpClientAutoConfiguration {

	private final HttpClientProperties properties;

	@Bean
	HttpClient httpClient(ApplicationContext context) {
		var builder = HttpClient.newBuilder();

		if (properties.executor != null) {
			builder.executor(context.getBean(properties.executor, Executor.class));
		}

		if (properties.proxy != null) {
			var hostPort = properties.proxy.split(":", 2);
			var address = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
			builder.proxy(ProxySelector.of(address));
		}

		if (properties.timeout != null) {
			builder.connectTimeout(properties.timeout);
		}

		return builder.followRedirects(HttpClient.Redirect.NORMAL).build();
	}
}
