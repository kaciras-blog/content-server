package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;

@Configuration
class FriendConfiguration {

	private final RedisTemplate<String, String> listAndMap;
	private final RedisTemplate<String, ValidateRecord> validate;

	public FriendConfiguration(RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		var hfs = new Jackson2JsonRedisSerializer<>(FriendLink.class);
		hfs.setObjectMapper(objectMapper);

		listAndMap = new RedisTemplate<>();
		listAndMap.setConnectionFactory(redisFactory);
		listAndMap.setDefaultSerializer(RedisSerializer.string());
		listAndMap.setHashValueSerializer(hfs);
		listAndMap.afterPropertiesSet();

		var hvs = new Jackson2JsonRedisSerializer<>(ValidateRecord.class);
		hvs.setObjectMapper(objectMapper);

		validate = new RedisTemplate<>();
		validate.setConnectionFactory(redisFactory);
		validate.setDefaultSerializer(RedisSerializer.string());
		validate.setHashValueSerializer(hvs);
		validate.afterPropertiesSet();
	}

	@Bean
	RedisList<String> hostList() {
		return new DefaultRedisList<>(RedisKeys.Friends.of("list"), listAndMap);
	}

	@Bean
	RedisMap<String, FriendLink> friendMap() {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("map"), listAndMap);
	}

	@Bean
	RedisMap<String, ValidateRecord> validateRecords() {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("validate"), validate);
	}
}
