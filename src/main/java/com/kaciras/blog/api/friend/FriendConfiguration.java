package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import lombok.RequiredArgsConstructor;
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

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class FriendConfiguration {

	private final RedisConnectionFactory redisFactory;
	private final ObjectMapper objectMapper;

	@Bean
	RedisTemplate<String, FriendLink> localRedisTemplate() {
		var template = new RedisTemplate<String, FriendLink>();
		template.setConnectionFactory(redisFactory);
//		template.setEnableTransactionSupport(true);

		template.setKeySerializer(RedisSerializer.string());

		var fs = new Jackson2JsonRedisSerializer<>(FriendLink.class);
		fs.setObjectMapper(objectMapper);
		template.setDefaultSerializer(fs);

		var vrs = new Jackson2JsonRedisSerializer<>(ValidateRecord.class);
		vrs.setObjectMapper(objectMapper);
		template.setHashValueSerializer(vrs);

		return template;
	}

	@Bean
	RedisList<FriendLink> friendList(RedisTemplate<String, FriendLink> redis) {
		return new DefaultRedisList<>(RedisKeys.Friends.of("list"), redis);
	}

	@Bean
	RedisMap<String, ValidateRecord> validateRecords(RedisTemplate<String, FriendLink> redis) {
		return new DefaultRedisMap<>(RedisKeys.Friends.of("vr"), redis);
	}
}
