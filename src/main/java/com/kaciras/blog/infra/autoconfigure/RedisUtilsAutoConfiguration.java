package com.kaciras.blog.infra.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.RedisOperationsBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration(after = {JacksonAutoConfiguration.class, RedisAutoConfiguration.class})
public class RedisUtilsAutoConfiguration {

	@Bean
	RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(factory);
		template.setDefaultSerializer(RedisSerializer.json());
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		return template;
	}

	@ConditionalOnClass({ObjectMapper.class, RedisConnectionFactory.class})
	@Bean
	RedisOperationsBuilder redisOperationsBuilder(ObjectMapper mapper, RedisConnectionFactory factory) {
		return new RedisOperationsBuilder(factory, mapper);
	}
}
