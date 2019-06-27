package net.kaciras.blog.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@RequiredArgsConstructor
@Repository
public class ConfigRepository {

	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	private final String namespace = RedisKeys.ConfigStore.value();

	public void save(String name, Object config) {
		try {
			redisTemplate.opsForValue().set(namespace + name, objectMapper.writeValueAsBytes(config));
		} catch (JsonProcessingException e) {
			throw new SerializationException("配置保存失败", e);
		}
	}

	/**
	 * 从配置存储中加载指定的配置对象。
	 * 多次调用应当返回不同（包括嵌套对象）的实例，修改任一实例的任何属性不能影响其他实例（类似深拷贝）。
	 *
	 * @param name 配置名
	 * @param type 配置对象的类型
	 * @param <T>  配置对象的类型
	 * @return 配置对象，如果没有就返回null
	 */
	public <T> T load(String name, Class<T> type) {
		var data = redisTemplate.opsForValue().get(namespace + name);
		if (data == null) {
			return null;
		}
		try {
			return objectMapper.readValue(data, type);
		} catch (IOException e) {
			throw new SerializationException("配置读取失败", e);
		}
	}
}
