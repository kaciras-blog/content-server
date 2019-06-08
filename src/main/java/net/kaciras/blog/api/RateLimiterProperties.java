package net.kaciras.blog.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kaciras.rate-limit")
@Getter
@Setter
public final class RateLimiterProperties {

	/**
	 * 全部请求限流，感觉跟DOS防御有重合。
	 * 目前单页最大4个请求，保守起见 *4 倍，每秒 2 个也比较高了。
	 */
	private TokenBucket generic = new TokenBucket(2, 16);

	/** 针对有副作用的请求（POST,PUT等）的限流，防刷评论 */
	private TokenBucket effective = new TokenBucket(0.5,4);

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	public static final class TokenBucket {

		/** 允许的速率（令牌/秒），必须大于0 */
		private double rate;

		/** 令牌桶容量，同时也是单位时间能获取的上限（令牌），必须大于0 */
		private int bucketSize;
	}
}
