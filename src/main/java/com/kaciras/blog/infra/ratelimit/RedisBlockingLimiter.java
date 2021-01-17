package com.kaciras.blog.infra.ratelimit;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * RateLimiter 的装饰类，可以增强被包装的限流器，在其拒绝时延长等待时间，并提供一些额外功能：
 * <ul>
 * <li>封禁延长：在该类的封禁时间内再次访问，则重置封禁时间，防止不断访问测试解封。</li>
 * <li>逐次递增：解封之后还有观察期，如果在此期间又触发了内部限流器的拒绝，下次封禁的时间会更长。</li>
 * </ul>
 * <p>
 * 该类不会改变最大访问速率，因为它取决于内部的限流器。但它可以治那些完全不懂得限速的自动访问软件。
 */
@RequiredArgsConstructor
public final class RedisBlockingLimiter implements RateLimiter {

	private final String namespace;
	private final RateLimiter inner;
	private final RedisConnectionFactory redisFactory;
	private final Clock clock;

	/**
	 * 封禁时间列表，索引从小到大封禁等级递增，当观察期内再次触发内层限流器拒绝将使用高一级的封禁时间。
	 * <p>
	 * 观察期时长为下一级的封禁时长，例如本次封禁时长为 blockTimes.get(1)，则观察期就是 blockTimes.get(2)，
	 * 观察期内再次封禁的时间长为 blockTimes.get(2)，此时观察期也升级至 blockTimes.get(3)。
	 * <p>
	 * 如果该列表为空，则本限流器无作用，仅代理到内层。
	 * 如果封禁时长已达到该列表的末尾，则无观察期，也不会再升级。
	 * 如果无观察期或在观察期内没有再触发内层拒绝，则等级重置，下次封禁从第一级开始。
	 */
	private List<Duration> blockTimes = Collections.emptyList();

	/** 是否启用封禁延长 */
	@Setter
	private boolean refreshOnReject;

	/**
	 * 设置封禁时间列表，列表中从前到后的等级逐渐升高，后面的时长必须大于前面的，时间都不能为负。
	 *
	 * @param blockTimes 封禁时间列表
	 * @throws IllegalArgumentException 如果参数不满足上述要求
	 */
	public void setBlockTimes(List<Duration> blockTimes) {
		var max = Duration.ZERO;
		for (var time : blockTimes) {
			if (time.compareTo(max) < 0) {
				throw new IllegalArgumentException("封禁时间存在负数，或小于前面的");
			}
			max = time;
		}
		this.blockTimes = List.copyOf(blockTimes);
	}

	/*
	 * 【更新】之前版本使用了异步化机制，将对内层限流器的调用和设置封禁记录这两操作放在其他线程中，
	 * 可以减少请求的执行时间。后来移除了，因为并非所有的限流算法都耗时较大，对于个别需要的算法可
	 * 以在其内部自己实现，或是做个异步装饰器类。
	 */
	@Override
	public long acquire(@NonNull String id, int permits) {
		if (blockTimes.isEmpty()) {
			return inner.acquire(id, permits);
		}

		// Why RedisConnection not implements AutoClosable? Is it reminds user to call RedisConnectionUtils?
		var connection = redisFactory.getConnection();
		try {
			return doAcquire(connection, id, permits);
		} finally {
			RedisConnectionUtils.releaseConnection(connection, redisFactory);
		}
	}

	/*
	 * 【记录的一致性】
	 * 这里的流程是 读取 -> 判断 -> 修改，存在与多线程 intValue++ 类似的异步问题。
	 * 麻烦的是 inner.acquire 不能撤销或是做两段提交，整个流程无法重试，也就意味着没法做CAS。
	 *
	 * 目前因为没什么访问量不要紧，所以没有解决此问题。
	 */
	private long doAcquire(RedisConnection connection, String id, int permits) {

		// 32 位秒数最大 2038 年，本代码肯定用不到那么久
		var now = (int) clock.instant().getEpochSecond();

		var blockKey = (namespace + id).getBytes(StandardCharsets.UTF_8);
		var record = deserialize(connection.get(blockKey));

		if (record != null) {
			var bTime = record.getBlockingTime();
			var waitTime = record.beginTime + bTime - now;

			if (waitTime > 0) {
				if (refreshOnReject) {
					record.beginTime = now;
					waitTime = bTime;
					connection.setEx(blockKey, bTime, record.serialize());
				}
				return waitTime;
			}
		}

		var waitTime = inner.acquire(id, permits);
		if (waitTime <= 0) {
			return waitTime;
		}
		if (record == null) {
			record = new BlockingRecord(0, now);
		} else {
			record.increaseLevel(now);
		}

		connection.setEx(blockKey, record.getObservationPeriod(), record.serialize());
		return record.getBlockingTime();
	}

	private BlockingRecord deserialize(@Nullable byte[] data) {
		if (data == null) {
			return null;
		}
		var buffer = ByteBuffer.wrap(data);
		return new BlockingRecord(buffer.getInt(), buffer.getInt());
	}

	@AllArgsConstructor
	private final class BlockingRecord {

		/** 当前的封禁等级，也是 blockTimes 的索引 */
		private int level;

		/** 封禁开始的时间（EpochSecond） */
		private int beginTime;

		private long getBlockingTime() {
			return blockTimes.get(level).toSeconds();
		}

		private long getObservationPeriod() {
			return blockTimes.get(Math.min(level + 1, blockTimes.size() - 1)).toSeconds();
		}

		private void increaseLevel(int now) {
			beginTime = now;
			level = Math.min(level + 1, blockTimes.size() - 1);
		}

		private byte[] serialize() {
			return ByteBuffer.allocate(8).putInt(level).putInt(beginTime).array();
		}
	}
}
