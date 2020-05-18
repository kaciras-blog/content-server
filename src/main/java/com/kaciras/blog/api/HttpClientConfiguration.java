package com.kaciras.blog.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

/**
 * 虽然Spring的RestTemplate也不错，但我还是喜欢原生的HttpClient啦
 */
@Configuration(proxyBeanMethods = false)
public class HttpClientConfiguration {

	@Bean
	HttpClient httpClient(ThreadPoolTaskScheduler threadPool, Environment env) {
		var builder = HttpClient.newBuilder().executor(threadPool);

		var proxy = env.getProperty("app.http-client-proxy");
		if (proxy != null) {
			var pair = proxy.split(":", 2);
			builder.proxy(ProxySelector.of(new InetSocketAddress(pair[0], Integer.parseInt(pair[1]))));
		}

		return builder.build();
	}
}
