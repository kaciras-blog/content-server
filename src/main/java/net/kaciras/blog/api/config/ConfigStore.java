package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.util.Iterator;

@RequiredArgsConstructor
@Repository
public class ConfigStore implements Iterable<ConfigStore.Property> {

	private final RedisTemplate<String, byte[]> redisTemplate;

	public void save(String name, Object value) {

	}

	@Override
	public Iterator<Property> iterator() {
		var options = ScanOptions.scanOptions()
				.match(RedisKeys.AccountSessions.of("*"))
				.build();
		var conn = redisTemplate.getRequiredConnectionFactory().getConnection();
		return null;
	}

	@RequiredArgsConstructor
	static final class Property {
		public final String key;
		public final String value;
	}
}
