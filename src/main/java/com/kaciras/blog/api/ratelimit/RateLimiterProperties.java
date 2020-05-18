package com.kaciras.blog.api.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.util.List;

/**
 * 限流器相关配置。
 *
 * 【新特性】
 * SpringBoot 2.2 支持 Properties 的构造方法注入，在类上加上 @ConstructorBinding 即可，支持递归。
 *
 * 【注意】
 * 为了 ConfigurationProcessor 自动生成文档，不能用 lombok 自动生成构造方法。
 */
@ConstructorBinding
@ConfigurationProperties("app.rate-limit")
public final class RateLimiterProperties {

	/** 全部请求限流，感觉跟DOS防御有重合 */
	public final TokenBucket generic;

	/** 针对有副作用的请求（POST,PUT等）的限流，防刷评论 */
	public final EffectiveLimiterConfig effective;

	public RateLimiterProperties(TokenBucket generic, EffectiveLimiterConfig effective) {
		this.generic = generic;
		this.effective = effective;
	}

	public static final class TokenBucket {

		/** 令牌桶容量，必须大于0 */
		public final int size;

		/** 允许的速率（令牌/秒），必须大于0 */
		public final double rate;

		public TokenBucket(int size, double rate) {
			this.size = size;
			this.rate = rate;
		}
	}

	/**
	 * 由时间和数量来创建令牌桶，意义是在 time 时间内允许 permits 个请求。
	 */
	public static final class RateLimit {

		/** 单位时间内允许的访问量 */
		public final int permits;

		/** 统计时间 */
		public final Duration time;

		public RateLimit(int permits, Duration time) {
			this.permits = permits;
			this.time = time;
		}

		/**
		 * 该类与 TokenBucket 可以互相转换。
		 *
		 * @return 等价的令牌桶
		 */
		public TokenBucket toTokenBucket() {
			return new TokenBucket(permits, permits / (double) time.toSeconds());
		}
	}

	public static final class EffectiveLimiterConfig {

		/** 针对副作用请求的多级限流的令牌桶列表 */
		public final List<RateLimit> limits;

		/** 针对副作用请求，在封禁期内再次访问则重新倒计时封禁时间 */
		public final boolean refreshOnReject;

		/** 针对副作用请求，在限流的基础上进一步增加封禁措施，封禁时间根据该列表依次递增 */
		public final List<Duration> blockTimes;

		public EffectiveLimiterConfig(List<RateLimit> limits, boolean refreshOnReject, List<Duration> blockTimes) {
			this.limits = limits;
			this.refreshOnReject = refreshOnReject;
			this.blockTimes = blockTimes;
		}
	}
}
