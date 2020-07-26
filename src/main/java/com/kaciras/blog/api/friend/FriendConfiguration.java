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

	private final RedisTemplate<String, String> template;

	public FriendConfiguration(RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		this.template = new RedisTemplate<>();
		template.setConnectionFactory(redisFactory);
		template.setEnableTransactionSupport(true);
		template.setDefaultSerializer(RedisSerializer.string());

		var hvs = new Jackson2JsonRedisSerializer<>(ValidateRecord.class);
		hvs.setObjectMapper(objectMapper);
		template.setHashValueSerializer(hvs);
		template.afterPropertiesSet();
	}

	@Bean
	RedisList<String> hostList() {
		return new DefaultRedisList<>(RedisKeys.Friends.of("list"), template);
	}

	@Bean
	RedisMap<String, FriendLink> friendMap() {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("map"), template);
	}

	@Bean
	RedisMap<String, ValidateRecord> validateRecords() {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("validate"), template);
	}
}
