package com.kaciras.blog.infra.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

final class ExtendsCodecModuleTest {

	private static final LocalDateTime TEST_TIME = LocalDateTime.of(2019, 8, 26, 17, 21, 35, 123_000_000);
	private static final long TEST_TIME_MILLIS = 1566840095123L;

	@Test
	void serializeLocalDateTime() throws JsonProcessingException {
		var objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ExtendsCodecModule());

		var data = objectMapper.writeValueAsString(TEST_TIME);
		Assertions.assertThat(Long.parseLong(data)).isEqualTo(TEST_TIME_MILLIS);
	}

	@Test
	void deserializeLocalDateTime() throws IOException {
		var objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ExtendsCodecModule());

		var time = objectMapper.readValue(String.valueOf(TEST_TIME_MILLIS), LocalDateTime.class);
		Assertions.assertThat(time).isEqualTo(TEST_TIME);
	}
}
