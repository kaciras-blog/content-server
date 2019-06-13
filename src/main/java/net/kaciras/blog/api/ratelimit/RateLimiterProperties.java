package net.kaciras.blog.api.ratelimit;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties("kaciras.rate-limit")
@Setter
public final class RateLimiterProperties {

	/** 全部请求限流，感觉跟DOS防御有重合 */
	public TokenBucket generic;

	/** 针对有副作用的请求（POST,PUT等）的限流，防刷评论 */
	public EffectiveLimiterConfig effective;

	@Setter
	public static final class TokenBucket {

		/** 允许的速率（令牌/秒），必须大于0 */
		public double rate;

		/** 令牌桶容量，同时也是单位时间能获取的上限（令牌），必须大于0 */
		public int size;
	}

	/**
	 * 由时间和数量来创建令牌桶，意义是在 time 时间内允许 permits 个请求。
	 *
	 * 该类与 TokenBucket 可以互相转换：
	 *   TokenBucket.rate = RateLimit.permits / (double)RateLimit.time.toSeconds()
	 *   TokenBucket.bucketSize = RateLimit.permits
	 */
	@Setter
	public static final class RateLimit {
		public int permits;
		public Duration time;
	}

	@Setter
	public static final class EffectiveLimiterConfig {

		public boolean refreshOnReject;

		public Duration blockTime;

		public List<RateLimit> limits;
	}
}
