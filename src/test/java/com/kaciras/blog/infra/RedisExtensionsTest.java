package com.kaciras.blog.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RedisExtensionsTest.TestConfig.class)
final class RedisExtensionsTest {

	@Import(RedisAutoConfiguration.class)
	@Configuration(proxyBeanMethods = false)
	static class TestConfig {

		@Bean
		RedisOperations<String, Object> hash(RedisConnectionFactory factory) {
			var template = new RedisTemplate<String, Object>();
			template.setConnectionFactory(factory);
			template.setDefaultSerializer(RedisSerializer.string());
			return template;
		}
	}

	private static final String KEY = "test:hashUpdate";

	@Autowired
	private RedisOperations<String, Object> operations;

	@BeforeEach
	void cleanUp() {
		operations.unlink(KEY);
	}

	@Test
	void hashUpdate() {
		operations.opsForHash().put(KEY, "hk", "foo");
		var success = RedisExtensions.hsetx(operations, KEY, "hk", "bar");

		assertThat(success).isTrue();
		assertThat(operations.opsForHash().get(KEY, "hk")).isEqualTo("bar");
	}

	@Test
	void updateNonExists() {
		var success = RedisExtensions.hsetx(operations, KEY, "hk", "bar");

		assertThat(success).isFalse();
		assertThat(operations.hasKey(KEY)).isFalse();
	}
}
