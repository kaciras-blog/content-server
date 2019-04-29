package net.kaciras.blog.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@RequiredArgsConstructor
@Repository
public class ConfigRepository {

	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	public void save(String name, Object config) {
		try {
			redisTemplate.opsForValue().set(name, objectMapper.writeValueAsBytes(config));
		} catch (JsonProcessingException e) {
			throw new SerializationException("配置保存失败", e);
		}
	}

	public <T> T load(String name, Class<T> type) {
		try {
			return objectMapper.readValue(redisTemplate.opsForValue().get(name), type);
		} catch (IOException e) {
			throw new SerializationException("配置读取失败", e);
		}
	}
}
