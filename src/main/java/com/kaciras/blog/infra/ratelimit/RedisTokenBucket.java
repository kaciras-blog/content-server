package com.kaciras.blog.infra.ratelimit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.lang.NonNull;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;

/**
 * 令牌桶算法的实现，使用Redis存储相关记录，该类里可以包含多个令牌桶。
 * 该类仅作为 Java 语言的接口，算法的实现在 Lua 脚本里，由 Redis 执行。
 * <p>
 * Spring Data Redis 里的 ScriptExecutor 跟 RedisTemplate 绑死了，很难直接基于 Connection 实现
 */
public final class RedisTokenBucket implements RateLimiter {

	private static final DefaultRedisScript<Long> SCRIPT;

	static {
		SCRIPT = new DefaultRedisScript<>();
		SCRIPT.setResultType(Long.class);
		SCRIPT.setLocation(new ClassPathResource("TokenBucket.lua"));
	}

	/*
	 * 经过一番思考，还是决定将命名空间放在限流器对象里（而不是上层）：
	 *   1.命名空间应当看作限流器的一部分，用于标识键的类型以跟其他数据隔离
	 *   2.如果需要进一步区分，则可以在id参数上做修改
	 *   3.如果使用装饰模式来扩展，则必须要在实例里对命名空间做区分
	 *   4.对于其他限流算法如简单计数等，不存在命名空间，要求在调用方对id做处理是多余的
	 */
	private final String namespace;
	private final Clock clock;
	private final RedisOperations<String, Object> redis;

	private Object[] bArgs = new Object[0];

	/** Redis 键的过期时间，该值是由容量和速率来计算的 */
	private int ttl;

	/** 最小的一个桶的容量 */
	private int minSize = Integer.MAX_VALUE;

	/**
	 * 创建 RedisTokenBucket 的新实例。
	 *
	 * @param namespace Redis键的前缀，用于多个限流器之间区分
	 * @param redis     Redis配置
	 * @param clock     用于获取当前时间，可以Mock该参数以便测试
	 */
	public RedisTokenBucket(String namespace, RedisOperations<String, Object> redis, Clock clock) {
		this.namespace = namespace;
		this.redis = redis;
		this.clock = clock;
	}

	/**
	 * 添加一个令牌桶，该桶具有指定的容量和填充速率。
	 *
	 * @param size 桶容量
	 * @param rate 填充速率（令牌/秒）
	 * @throws IllegalArgumentException 如果 size 或 rate 的取值范围错误
	 */
	public void addBucket(int size, double rate) {
		if (size < 0) {
			throw new IllegalArgumentException("size cannot be negative");
		}
		if (rate <= 0) {
			throw new IllegalArgumentException("rate must be greater than 0");
		}

		bArgs = Arrays.copyOf(bArgs, bArgs.length + 2);
		bArgs[bArgs.length - 2] = size;
		bArgs[bArgs.length - 1] = rate;

		ttl = Math.max(ttl, (int) Math.ceil(size / rate));
		minSize = Math.min(minSize, size);
	}

	/**
	 * 遍历所有的令牌桶，从每个桶中都要取走指定数量的令牌。
	 * <p>
	 * 当所有令牌桶内都有充足的令牌时返回0，否则返回需要等待的时间。
	 * 如果多个令牌桶的令牌都不足，则返回等待时间最长的。
	 * 只要返回非零值，则所有令牌桶都不会被修改，本次请求不造成任何影响。
	 * <p>
	 * permits 小于等于0的情况没有处理，调用方自己考虑其意义
	 */
	public long acquire(@NonNull String id, int permits) {
		if (permits > minSize) {
			return -1;
		}

		// id 已用 @NonNull，不再做运行期检查
		var keys = Collections.singletonList(namespace + id);

		var args = new Object[3 + bArgs.length];
		args[0] = permits;
		args[1] = clock.instant().getEpochSecond();
		args[2] = ttl;
		System.arraycopy(bArgs, 0, args, 3, bArgs.length);

		// 仅在连接处于 Pipeline 和 Queue 状态下才会返回空值
		// noinspection ConstantConditions
		return redis.execute(SCRIPT, keys, args);
	}
}
