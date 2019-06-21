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

@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration
@RequiredArgsConstructor
public class RateLimiterAutoConfiguration {

	private final RateLimiterProperties properties;
	private final Clock clock;

	@ConditionalOnMissingBean
	@Bean
	RedisTemplate<String, Object> genericToStringRedisTemplate(RedisConnectionFactory factory) {
		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setEnableDefaultSerializer(false);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		return redisTemplate;
	}

	// 在这里决定Checker的顺序
	@Bean
	RateLimitFilter rateLimitFilter(RedisTemplate<String, Object> redis) {
		var checkers = new ArrayList<RateLimiterChecker>(2);
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
		for (var limit : config.limits) {
			inner.addBucket(limit.permits, limit.permits / (double) limit.time.toSeconds());
		}

		var limiter = new RedisBlockingLimiter(RedisKeys.EffectBlocking.value(), inner, redis);
		limiter.setBanTime(config.blockTime);
		limiter.setRefreshOnReject(config.refreshOnReject);

		return new EffectRateChecker(limiter);
	}
}
