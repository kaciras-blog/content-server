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

/**
 * 基于令牌桶算法的限速器，使用Redis存储相关记录。
 * 该类仅作为Java语言的接口，算法的具体实现在Lua脚本(resources/RateLimiter.lua)里由Redis执行
 */
@Setter
public final class RedisRateLimiter {

	private static final String SCRIPT_PATH = "/RateLimiter.lua";

	private final Clock clock;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisScript<Long> script;

	private int bucketSize;
	private double rate;
	private int cacheTime;

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
		script.setLocation(new ClassPathResource(SCRIPT_PATH));
		this.script = script;
	}

	/**
	 * 获取指定数量的令牌，返回桶内拥有足够令牌所需要等待的时间，返回0表示获取成功。
	 *
	 * @param key 表示获取者的键，一般是对方的IP或ID之类的
	 * @param permits 要获取的令牌数量
	 * @return 需要等待的时间（秒）
	 */
	public long acquire(String key, int permits) {
		if (permits > bucketSize) {
			throw new IllegalArgumentException("所需令牌数大于桶的容量");
		}
		var now = clock.instant().getEpochSecond();
		var waitTime = redisTemplate.execute(script, List.of(key), permits, now, bucketSize, rate, cacheTime);

		if (waitTime == null) {
			throw new AssertionError("限速脚本返回了空值，ID=" + key);
		}
		return waitTime;
	}
}
