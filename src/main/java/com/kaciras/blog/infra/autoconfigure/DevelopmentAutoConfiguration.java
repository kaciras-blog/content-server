package com.kaciras.blog.infra.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * 帮助开发调试的一些工具，包括请求延时、关闭身份验证等。
 */
@RequiredArgsConstructor
@ConditionalOnClass
@EnableConfigurationProperties(DevelopmentProperties.class)
@Configuration(proxyBeanMethods = false)
public class DevelopmentAutoConfiguration {

	private final DevelopmentProperties properties;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@ConditionalOnProperty("app.development.http-delay")
	@Bean
	public Filter delayFilter() {
		var millis = properties.getHttpDelay().toMillis();
		return (request, response, chain) -> {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			chain.doFilter(request, response);
		};
	}
}
