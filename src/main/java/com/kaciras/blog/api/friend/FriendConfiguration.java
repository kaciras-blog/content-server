package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;

@Configuration(proxyBeanMethods = false)
class FriendConfiguration {

	private final RedisTemplate<String, ValidateRecord> validate;

	public FriendConfiguration(RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		var hvs = new Jackson2JsonRedisSerializer<>(ValidateRecord.class);
		hvs.setObjectMapper(objectMapper);

		validate = new RedisTemplate<>();
		validate.setConnectionFactory(redisFactory);
		validate.setDefaultSerializer(RedisSerializer.string());
		validate.setHashValueSerializer(hvs);
		validate.afterPropertiesSet();
	}

	@Bean
	RedisMap<String, ValidateRecord> validateMap() {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("validate"), validate);
	}
}
