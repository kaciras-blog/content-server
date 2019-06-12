package net.kaciras.blog.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("kaciras.rate-limit")
@Getter
@Setter
public final class RateLimiterProperties {

	/**
	 * 全部请求限流，感觉跟DOS防御有重合。
	 */
	private TokenBucket generic;

	/** 针对有副作用的请求（POST,PUT等）的限流，防刷评论 */
	private List<TokenBucket> effective;

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
