package com.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ConfigBindingConfiguration {

	private final GenericApplicationContext context;

	@Bean
	RedisTemplate<String, byte[]> rawRedisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, byte[]>();
		template.setEnableDefaultSerializer(false);
		template.setConnectionFactory(factory);
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		return template;
	}

	@Bean
	public ConfigBindingPostProcessor bindingPostProcessor(ConfigBindingManager service) {
		return new ConfigBindingPostProcessor(context, service);
	}
}
