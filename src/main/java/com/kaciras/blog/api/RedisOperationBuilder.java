package com.kaciras.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@RequiredArgsConstructor
public final class RedisOperationBuilder {

	private final RedisConnectionFactory factory;
	private final ObjectMapper objectMapper;

	private <T> Jackson2JsonRedisSerializer<T> jsonSerializer(Class<T> type) {
		var serializer = new Jackson2JsonRedisSerializer<T>(type);
		serializer.setObjectMapper(objectMapper);
		return serializer;
	}

	private <V> RedisTemplate<String, V> newTemplate() {
		var template = new RedisTemplate<String, V>();
		template.setConnectionFactory(factory);
		template.setDefaultSerializer(RedisSerializer.string());
		return template;
	}

	public <V> BoundHashOperations<String, String, V> bindHash(String key, Class<V> type) {
		var template = this.<V>newTemplate();
		template.setHashValueSerializer(jsonSerializer(type));
		template.afterPropertiesSet();
		return template.boundHashOps(key);
	}

	public <V> BoundListOperations<String, V> bindList(String key, Class<V> type) {
		return bindList(key, jsonSerializer(type));
	}

	public <V> BoundListOperations<String, V> bindList(String key, RedisSerializer<V> serializer) {
		var template = this.<V>newTemplate();
		template.setValueSerializer(serializer);
		template.afterPropertiesSet();
		return template.boundListOps(key);
	}
}
