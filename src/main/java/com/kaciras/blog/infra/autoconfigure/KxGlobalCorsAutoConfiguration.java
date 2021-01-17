package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.autoconfigure.CorsProperties.CorsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 配置全局的 CORS 处理器，使用 CorsFilter 拦截器。
 * 如果应用不需要区分不同路由的 CORS 则使用此类可以方便的处理。
 *
 * TODO: CORS 并不是经常改变或是具有通用设置的东西，搞这个类是否多余？
 */
@EnableConfigurationProperties(CorsProperties.class)
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class KxGlobalCorsAutoConfiguration {

	// 尽量提早过滤掉无效的请求
	private static final int FILTER_ORDER = Integer.MIN_VALUE + 10;

	private final CorsProperties properties;

	private void configure(CorsConfiguration config) {
		if (properties.getTemplate() == CorsTemplate.Default) {
			config.applyPermitDefaultValues();
		} else if (properties.getTemplate() == CorsTemplate.AllowAll) {
			var all = List.of(CorsConfiguration.ALL);
			config.setAllowedMethods(all);
			config.setAllowedHeaders(all);
			config.setAllowedOriginPatterns(all);
			config.setAllowCredentials(true);
		}

		if (properties.getAllowedOrigins() != null) {
			config.setAllowedOrigins(properties.getAllowedOrigins());
		}
		if (properties.getAllowedOrigins() != null) {
			config.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
		}

		// 以下这些配置通常在开发和生产环境之间也不会变化。
		if (properties.getAllowCredentials() != null) {
			config.setAllowCredentials(properties.getAllowCredentials());
		}
		if (properties.getMaxAge() != null) {
			config.setMaxAge(properties.getMaxAge());
		}
		if (properties.getAllowedMethods() != null) {
			config.setAllowedMethods(properties.getAllowedMethods());
		}
		if (properties.getAllowedHeaders() != null) {
			config.setAllowedHeaders(properties.getAllowedHeaders());
		}
		if (properties.getExposedHeaders() != null) {
			config.setExposedHeaders(properties.getExposedHeaders());
		}
	}

	// 因为要设置优先级所以使用了 FilterRegistrationBean 来包装
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		var source = new UrlBasedCorsConfigurationSource();
		var config = new CorsConfiguration();
		configure(config);
		source.registerCorsConfiguration("/**", config);

		var registration = new FilterRegistrationBean<CorsFilter>();
		registration.setOrder(FILTER_ORDER);
		registration.setFilter(new CorsFilter(source));
		return registration;
	}
}
