package com.kaciras.blog.infra;

import com.kaciras.blog.infra.ratelimit.RedisTokenBucket;
import com.kaciras.blog.infra.ratelimit.TestRedisConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * 测量 RedisTokenBucket 的性能，其 acquire 方法包括三个方面的开销：JAVA层逻辑、通信开销、Redis脚本执行时间。
 * 如果单独衡量 TokenBucket.lua 脚本的性能，请使用 redis-benchmark 来测，结果见 resource/TokenBucketBenchmark.txt
 * <p>
 * Benchmark                       Mode  Cnt    Score    Error  Units
 * RedisTokenBucketPerf.buckets1   avgt    5  319.992 ±  7.507  us/op
 * RedisTokenBucketPerf.buckets40  avgt    5  461.470 ± 19.464  us/op
 */
@SuppressWarnings({"UnusedReturnValue", "unchecked"})
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@Measurement(iterations = 5, time = 10)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RedisTokenBucketPerf {

	private static final String NAMESPACE = "TokenBucket:";
	private static final String KEY = "Benchmark";

	private ConfigurableApplicationContext context;

	private RedisTokenBucket single;
	private RedisTokenBucket forty;

	@Setup
	public void setUp() {
		context = new SpringApplicationBuilder(TestRedisConfiguration.class).web(WebApplicationType.NONE).run();
	}

	@Setup(Level.Iteration)
	public void setUpIteration() {
		var template = (RedisTemplate<String, Object>) context.getBean("testRedisTemplate");
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

	@TearDown
	public void tearDown() {
		context.close();
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

	public static void main(String[] args) throws Exception {
		var options = new OptionsBuilder()
				.include(RedisTokenBucketPerf.class.getSimpleName())
				.build();
		new Runner(options).run();
	}
}
