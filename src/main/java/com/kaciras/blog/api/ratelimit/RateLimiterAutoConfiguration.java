package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.infra.ratelimit.RedisBlockingLimiter;
import com.kaciras.blog.infra.ratelimit.RedisTokenBucket;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Clock;
import java.util.ArrayList;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterAutoConfiguration {

	private final RateLimiterProperties properties;

	private final RedisConnectionFactory factory;
	private final Clock clock;

	private final RedisTemplate<String, Object> redis;

	public RateLimiterAutoConfiguration(RateLimiterProperties properties, RedisConnectionFactory factory, Clock clock) {
		this.properties = properties;
		this.factory = factory;
		this.clock = clock;

		redis = new RedisTemplate<>();
		redis.setConnectionFactory(factory);
		redis.setEnableDefaultSerializer(false);
		redis.setKeySerializer(RedisSerializer.string());
		redis.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		redis.afterPropertiesSet();
	}

	@Bean
	RateLimitFilter rateLimitFilter() {
		var checkers = new ArrayList<RateLimiterChecker>(2);

		// 这里决定Checker的顺序，先通用后特殊
		if (properties.generic != null) {
			checkers.add(createGenericChecker());
		}
		if (properties.effective != null) {
			checkers.add(createEffectChecker());
		}
		return new RateLimitFilter(checkers);
	}

	private RateLimiterChecker createGenericChecker() {
		var limiter = new RedisTokenBucket(RedisKeys.RateLimit.value(), redis, clock);
		var bucket = properties.generic;

		if (bucket.size == 0) {
			throw new IllegalArgumentException("令牌桶容量不能为0");
		}
		if (bucket.rate == 0) {
			throw new IllegalArgumentException("令牌桶添加速率不能为0");
		}
		limiter.addBucket(bucket.size, bucket.rate);

		return (ip, request) -> limiter.acquire(ip.toString(), 1);
	}

	private EffectRateChecker createEffectChecker() {
		var config = properties.effective;

		var inner = new RedisTokenBucket(RedisKeys.EffectRate.value(), redis, clock);
		for (var limit : config.limits) {
			var bucket = limit.toTokenBucket();
			inner.addBucket(bucket.size, bucket.rate);
		}

		var wrapper = new RedisBlockingLimiter(RedisKeys.EffectBlocking.value(), inner, factory, clock);
		wrapper.setBlockTimes(config.blockTimes);
		wrapper.setRefreshOnReject(config.refreshOnReject);

		return new EffectRateChecker(wrapper);
	}
}
