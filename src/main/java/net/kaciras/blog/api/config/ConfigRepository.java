package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Repository
public class ConfigRepository {

	private final RedisTemplate<String, String> redisTemplate;

	public void save(Map<String, String> properties) {
		redisTemplate.opsForValue().multiSet(properties);
	}

	public List<String> load(List<String> keys) {
		return redisTemplate.opsForValue().multiGet(keys);
	}

	public Stream<Property> loadAll() {
		var options = ScanOptions.scanOptions().match("cfg:*").build();
		var connection = redisTemplate.getRequiredConnectionFactory().getConnection();

		var spliterator = Spliterators.spliteratorUnknownSize(connection.scan(options), Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false).map(this::mapToProperty);
	}

	private Property mapToProperty(byte[] key) {
		var value = redisTemplate.opsForValue().get(key);
		if (value == null) {
			throw new DataIntegrityViolationException("Redis遍历时返回了不存在的键？");
		}
		return new Property(new String(key, StandardCharsets.UTF_8), value);
	}

	@RequiredArgsConstructor
	static final class Property {
		public final String key;
		public final String value;
	}
}
