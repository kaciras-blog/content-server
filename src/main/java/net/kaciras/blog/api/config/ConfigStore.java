package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Repository
public class ConfigStore implements Iterable<ConfigStore.Property> {


	private final RedisTemplate<byte[], byte[]> redisTemplate;

	public void save(String name, Object value) {

	}

	public Map<String, Object> load(List<String> keys) {
		return null;
	}

	@Override
	public Iterator<Property> iterator() {
		var options = ScanOptions.scanOptions().match("cfg:*").build();
		var connection = redisTemplate.getRequiredConnectionFactory().getConnection();

		var spliterator = Spliterators.spliteratorUnknownSize(connection.scan(options), Spliterator.ORDERED);
		var stream = StreamSupport.stream(spliterator, false)
				.map(bytes -> redisTemplate.opsForValue().get(bytes));
		return null;
	}

	@RequiredArgsConstructor
	static final class Property {
		public final String key;
		public final String value;
	}
}
