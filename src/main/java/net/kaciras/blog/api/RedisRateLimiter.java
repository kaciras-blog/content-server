package net.kaciras.blog.api;

import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Clock;
import java.util.List;

@Setter
public final class RedisRateLimiter {

	private final Clock clock;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisScript<Long> script;

	private int bucketSize = 5;
	private double rate = 0.1;
	private int cacheTime = 60;

	public RedisRateLimiter(Clock clock, RedisConnectionFactory factory) {
		this.clock = clock;

		redisTemplate = new RedisTemplate<>();
		redisTemplate.setEnableDefaultSerializer(false);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.afterPropertiesSet();

		var script = new DefaultRedisScript<Long>();
		script.setResultType(Long.class);
		script.setLocation(new ClassPathResource("/RateLimiter.lua"));
		this.script = script;
	}

	public long acquire(String id, int permits) {
		if (permits > bucketSize) {
			throw new IllegalArgumentException("所需令牌数大于桶的容量");
		}
		var now = clock.instant().getEpochSecond();
		var keys = List.of(RedisKeys.RateLimit.of(id));

		var waitTime = redisTemplate.execute(script, keys, permits, now, bucketSize, rate, cacheTime);
		if (waitTime == null) {
			throw new AssertionError("限速脚本返回了空值，ID=" + id);
		}
		return waitTime;
	}
}
