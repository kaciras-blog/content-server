package com.kaciras.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * 鉴于 RedisTemplate 包含的太多，而实际使用时序列化方式与被存储对象高度相关，
 * 几乎每个地方都要创建自己的 RedisTemplate，所以搞了这个工具类来快速创建。
 */
@RequiredArgsConstructor
@Component
public final class RedisOperationsBuilder {

	private final RedisConnectionFactory factory;
	private final ObjectMapper objectMapper;

	private <T> Jackson2JsonRedisSerializer<T> jsonSerializer(Class<T> type) {
		var serializer = new Jackson2JsonRedisSerializer<>(type);
		serializer.setObjectMapper(objectMapper);
		return serializer;
	}

	private <V> RedisTemplate<String, V> newTemplate() {
		var template = new RedisTemplate<String, V>();
		template.setConnectionFactory(factory);
		template.setDefaultSerializer(RedisSerializer.string());
		return template;
	}

	/**
	 * 创建一个 HASH 类型的存储，使用字符串作为 HASH 的键，值使用 JSON 序列化。
	 *
	 * @param key 存储名
	 * @param type HASH 值的类型
	 * @param <V> HASH 值的类型
	 */
	public <V> BoundHashOperations<String, String, V> bindHash(String key, Class<V> type) {
		var template = this.<V>newTemplate();
		template.setHashValueSerializer(jsonSerializer(type));
		template.afterPropertiesSet();
		return template.boundHashOps(key);
	}

	/**
	 * 创建一个 LIST 类型的存储，其中的元素使用 JSON 序列化。
	 *
	 * @param key 存储名
	 * @param type 元素的类型
	 * @param <V> 元素的类型
	 */
	public <V> BoundListOperations<String, V> bindList(String key, Class<V> type) {
		return bindList(key, jsonSerializer(type));
	}

	/**
	 * 创建一个 LIST 类型的存储，其中的元素使用指定的序列化方式。
	 *
	 * @param key 存储名
	 * @param serializer 序列化方式
	 * @param <V> 元素的类型
	 */
	public <V> BoundListOperations<String, V> bindList(String key, RedisSerializer<V> serializer) {
		var template = this.<V>newTemplate();
		template.setValueSerializer(serializer);
		template.afterPropertiesSet();
		return template.boundListOps(key);
	}
}
