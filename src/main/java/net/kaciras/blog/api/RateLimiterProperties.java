package net.kaciras.blog.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kaciras.cors")
@Getter
@Setter
public final class RateLimiterProperties {

	/** 允许的速率（令牌/秒）*/
	private double rate = 1;

	/** 令牌桶容量（令牌）*/
	private int bucketSize = 8;

	/** 令牌桶在Redis里保存的时间（秒）*/
	private int cacheTime = 60;
}
