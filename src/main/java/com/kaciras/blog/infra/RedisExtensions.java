package com.kaciras.blog.infra;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collections;

/**
 * 实现了仅当HASH中存在该键的时候才修改值的命令。
 * <p>
 * 【吐槽】
 * 为什么 Redis 有 HSETNX 却没有对称的 HSETX，还非得拿 Lua 来做。
 * <p>
 * Redis作者说可以用事务实现，但是 CAS 需要重试代码多，完全不如内置一个命令好。
 * https://github.com/redis/redis/issues/441
 */
public final class RedisExtensions {

	private static final DefaultRedisScript<Boolean> SCRIPT;

	static {
		SCRIPT = new DefaultRedisScript<>();
		SCRIPT.setResultType(Boolean.class);
		SCRIPT.setLocation(new ClassPathResource("SetIfExists.lua"));
	}

	private RedisExtensions() {}

	// SpringDataRedis 这API都是怎么设计的？各种重载冲突、类型断档。
	@SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
	public static <K, HK, HV> Boolean hsetx(RedisOperations<K, ?> redis, K key, HK hkey, HV value) {
		var k = Collections.singletonList(key);

		var argv1 = ((RedisSerializer) redis.getHashKeySerializer()).serialize(hkey);
		var argv2 = ((RedisSerializer) redis.getHashValueSerializer()).serialize(value);

		var argSer = (RedisSerializer<?>) null;
		var resSer = (RedisSerializer<Boolean>) redis.getValueSerializer();

		return redis.execute(SCRIPT, argSer, resSer, k, argv1, argv2);
	}
}
