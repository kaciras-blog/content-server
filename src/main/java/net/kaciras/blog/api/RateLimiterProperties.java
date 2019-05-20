package net.kaciras.blog.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kaciras.cors")
@Getter
@Setter
public final class RateLimiterProperties {

	/** 允许的速率（令牌/秒）*/
	private double rate = 2;

	/** 令牌桶容量，同时也是单位时间能获取的上限（令牌）*/
	private int bucketSize = 16;
}
