package com.kaciras.blog.api.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

// TODO: SpringBoot 2.2 支持 Properties 的构造方法注入了

// 【注意】为了 Configuration Processor 自动生成文档，必须要有 Getter
@ConfigurationProperties("app.rate-limit")
@Setter
@Getter
public final class RateLimiterProperties {

	/** 全部请求限流，感觉跟DOS防御有重合 */
	public TokenBucket generic;

	/** 针对有副作用的请求（POST,PUT等）的限流，防刷评论 */
	public EffectiveLimiterConfig effective;

	@Setter
	@Getter
	public static final class TokenBucket {

		/** 令牌桶容量，必须大于0 */
		public int size;

		/** 允许的速率（令牌/秒），必须大于0 */
		public double rate;
	}

	/**
	 * 由时间和数量来创建令牌桶，意义是在 time 时间内允许 permits 个请求。
	 *
	 * 该类与 TokenBucket 可以互相转换：
	 *   TokenBucket.rate = RateLimit.permits / (double)RateLimit.time.toSeconds()
	 *   TokenBucket.bucketSize = RateLimit.permits
	 */
	@Setter
	@Getter
	public static final class RateLimit {
		public int permits;
		public Duration time;
	}

	@Setter
	@Getter
	public static final class EffectiveLimiterConfig {

		/** 针对副作用请求，在封禁期内再次访问则重新倒计时封禁时间 */
		public boolean refreshOnReject;

		/** 针对副作用请求，在限流的基础上进一步增加封禁措施，封禁时间根据该列表依次递增 */
		public List<Duration> blockTimes;

		/** 针对副作用请求的多级限流的令牌桶列表 */
		public List<RateLimit> buckets;
	}
}
