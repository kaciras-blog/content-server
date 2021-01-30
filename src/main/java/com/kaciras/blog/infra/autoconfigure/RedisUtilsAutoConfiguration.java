package com.kaciras.blog.infra.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.RedisOperationsBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({JacksonAutoConfiguration.class, RedisAutoConfiguration.class})
public class RedisUtilsAutoConfiguration {

	@ConditionalOnClass({ObjectMapper.class, RedisConnectionFactory.class})
	@Bean
	RedisOperationsBuilder redisOperationsBuilder(ObjectMapper mapper, RedisConnectionFactory factory) {
		return new RedisOperationsBuilder(factory, mapper);
	}
}
