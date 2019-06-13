package net.kaciras.blog.api.ratelimit;

import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.Executor;

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
	GenericRateLimitFilter genericRateLimiterFilter(Clock clock, RedisTemplate<String, Object> template) {
		var limiter = new RedisTokenBucket(clock, template);
		var bucket = properties.generic;
		limiter.addBucket(bucket.size, bucket.rate);
		return new GenericRateLimitFilter(limiter);
	}

	@Bean
	EffectRateLimitFilter effectRateLimitFilter(
			Clock clock,
			Executor threadPool,
			RedisTemplate<String, Object> oTemplate,
			RedisTemplate<String, byte[]> bTemplate) {

		var limiter = new RedisTokenBucket(clock, oTemplate);
		var filter = new EffectRateLimitFilter(limiter, bTemplate, threadPool);
		var config = properties.effective;

		filter.setBanTime(config.blockTime);
		filter.setRefreshOnReject(config.refreshOnReject);

		for (var limit : config.limits) {
			limiter.addBucket(limit.permits, limit.permits / (double)limit.time.toSeconds());
		}
		return filter;
	}
}
