package com.kaciras.blog.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaciras.blog.api.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
final class ConfigRepositoryTest {

	@Autowired
	private RedisConnectionFactory redis;

	@Autowired
	private ConfigRepository repository;

	@BeforeEach
	void flushDb() {
		redis.getConnection().flushDb();
	}

	@Test
	void loadInvalidData() {
		var data = "123456";
		redis.getConnection().set(RedisKeys.ConfigStore.of("test").getBytes(), data.getBytes());

		assertThatThrownBy(() -> repository.load("test", TestConfig.class))
				.isInstanceOf(SerializationException.class);
	}

	@Test
	void saveFailed() {
		assertThatThrownBy(() -> repository.save("test", new ThrowsConfig()))
				.isInstanceOf(SerializationException.class);
	}

	@Test
	void loadDefault() {
		var value = repository.load("test", TestConfig.class);
		assertThat(value).isNull();
	}

	@Test
	void save() {
		var value = new TestConfig();
		value.flag = true;
		repository.save("test", value);

		var loaded = repository.load("test", TestConfig.class);
		assertThat(loaded.flag).isTrue();
	}

	private static final class TestConfig {
		public boolean flag;
	}

	private static final class ThrowsConfig {

		@SuppressWarnings("unused")
		@JsonProperty
		public int getValue() throws IOException {
			throw new IOException("");
		}
	}
}
