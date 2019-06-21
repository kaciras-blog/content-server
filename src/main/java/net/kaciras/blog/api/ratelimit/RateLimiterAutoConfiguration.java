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

@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration
@RequiredArgsConstructor
public class RateLimiterAutoConfiguration {

	private final RateLimiterProperties properties;

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

	@Bean
	GenericRateLimitFilter genericRateLimiterFilter(RedisTemplate<String, Object> template, Clock clock) {
		var limiter = new RedisTokenBucket(RedisKeys.RateLimit.value(), template, clock);
		var bucket = properties.generic;
		limiter.addBucket(bucket.size, bucket.rate);
		return new GenericRateLimitFilter(limiter);
	}

	@Bean
	EffectRateLimitFilter effectRateLimitFilter(RedisTemplate<String, Object> template, Clock clock) {
		var inner = new RedisTokenBucket(RedisKeys.EffectRate.value(), template, clock);
		var config = properties.effective;

		for (var limit : config.limits) {
			inner.addBucket(limit.permits, limit.permits / (double) limit.time.toSeconds());
		}

		var limiter = new RedisBlockingLimiter(RedisKeys.EffectBlocking.value(), inner, template);
		return new EffectRateLimitFilter(limiter);
	}
}
