package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.RedisKeys;
import net.kaciras.blog.infrastructure.ratelimit.RedisBlockingLimiter;
import net.kaciras.blog.infrastructure.ratelimit.RedisTokenBucket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Clock;
import java.util.ArrayList;

// TODO: SpringBoot 2.2 可以扫描 ConfigurationProperties，但IDE还会报错
@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration
@RequiredArgsConstructor
public class RateLimiterAutoConfiguration {

	private final RedisConnectionFactory factory;
	private final Clock clock;

	private final RateLimiterProperties properties;

	@ConditionalOnMissingBean
	@Bean
	RedisTemplate<String, Object> genericToStringRedisTemplate() {
		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setEnableDefaultSerializer(false);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		return redisTemplate;
	}

	@Bean
	RateLimitFilter rateLimitFilter(RedisTemplate<String, Object> redis) {
		var checkers = new ArrayList<RateLimiterChecker>(2);

		// 这里决定Checker的顺序，先通用后特殊
		if (properties.generic != null) {
			checkers.add(createGenericChecker(redis));
		}
		if (properties.effective != null) {
			checkers.add(createEffectChecker(redis));
		}
		return new RateLimitFilter(checkers);
	}

	private GenericRateChecker createGenericChecker(RedisTemplate<String, Object> redis) {
		var limiter = new RedisTokenBucket(RedisKeys.RateLimit.value(), redis, clock);
		var bucket = properties.generic;
		limiter.addBucket(bucket.size, bucket.rate);
		return new GenericRateChecker(limiter);
	}

	private EffectRateChecker createEffectChecker(RedisTemplate<String, Object> redis) {
		var config = properties.effective;

		var inner = new RedisTokenBucket(RedisKeys.EffectRate.value(), redis, clock);
		for (var limit : config.buckets) {
			inner.addBucket(limit.permits, limit.permits / (double) limit.time.toSeconds());
		}

		var wrapper = new RedisBlockingLimiter(RedisKeys.EffectBlocking.value(), inner, factory, clock);
		wrapper.setBlockTimes(config.blockTimes);
		wrapper.setRefreshOnReject(config.refreshOnReject);

		return new EffectRateChecker(wrapper);
	}
}
