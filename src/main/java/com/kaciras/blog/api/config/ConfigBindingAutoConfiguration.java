package com.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.validation.Validator;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ConfigBindingAutoConfiguration {

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
	public ConfigBindingManager configService(ConfigRepository configRepository,
											  @Autowired(required = false) Validator validator) {
		return new ConfigBindingManager(configRepository, validator);
	}

	@Bean
	public ConfigBindingPostProcessor bindingPostProcessor(ConfigBindingManager service) {
		return new ConfigBindingPostProcessor(context, service);
	}
}
