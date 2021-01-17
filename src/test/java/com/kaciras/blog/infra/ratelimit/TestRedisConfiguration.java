package com.kaciras.blog.infra.ratelimit;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

// 使用专用的配置，避免加载整个应用
@Import(RedisAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class TestRedisConfiguration {

	@Bean
	RedisTemplate<String, Object> testRedisTemplate(RedisConnectionFactory factory) {
		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setEnableDefaultSerializer(false);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		return redisTemplate;
	}
}
