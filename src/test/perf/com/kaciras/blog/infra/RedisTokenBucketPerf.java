package com.kaciras.blog.infra;

import com.kaciras.blog.AbstractSpringPerf;
import com.kaciras.blog.infra.ratelimit.RedisTokenBucket;
import com.kaciras.blog.infra.ratelimit.TestRedisConfiguration;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * RedisTokenBucket 的性能包括三个方面的开销：JAVA层逻辑、通信开销、Redis脚本执行时间。
 * 如果单独测量 TokenBucket.lua，请使用 redis-benchmark，结果见 resource/TokenBucket.txt
 * <p>
 * Benchmark                       Mode  Cnt    Score    Error  Units
 * RedisTokenBucketPerf.buckets1   avgt    5  319.992 ±  7.507  us/op
 * RedisTokenBucketPerf.buckets40  avgt    5  461.470 ± 19.464  us/op
 */
@ContextConfiguration(classes = TestRedisConfiguration.class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Measurement(iterations = 5, time = 10)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RedisTokenBucketPerf extends AbstractSpringPerf {

	private static final String NAMESPACE = "TokenBucket:";
	private static final String KEY = "Benchmark";

	@Autowired
	private RedisTemplate<String, Object> template;

	private RedisTokenBucket single;
	private RedisTokenBucket forty;

	@Setup(Level.Iteration)
	public void setUpIteration() {
		template.unlink(KEY);

		single = new RedisTokenBucket(NAMESPACE, template, Clock.systemDefaultZone());
		single.addBucket(Integer.MAX_VALUE, 10_0000);

		forty = new RedisTokenBucket(NAMESPACE, template, Clock.systemDefaultZone());
		for (int i = 0; i < 20; i++) {
			forty.addBucket(10_0000, 10_0000);
		}
		for (int i = 0; i < 20; i++) {
			forty.addBucket(Integer.MAX_VALUE, 10_0000);
		}
	}

	// 下面测量包含1个桶和40个桶时的执行时间

	@Benchmark
	public long buckets1() {
		return single.acquire(KEY, 100);
	}

	@Benchmark
	public long buckets40() {
		return forty.acquire(KEY, 100);
	}
}
