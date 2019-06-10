package net.kaciras.blog.api;

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
	GenericRateLimitFilter rateLimiterFilter(Clock clock, RedisTemplate<String, Object> template) {
		var limiter = new RedisTokenBucket(clock, template);
		limiter.setRate(properties.getGeneric().getRate());
		limiter.setBucketSize(properties.getGeneric().getBucketSize());
		return new GenericRateLimitFilter(limiter);
	}

	@Bean
	EffectRateLimitFilter effectRateLimitFilter(
			Clock clock,
			Executor threadPool,
			RedisTemplate<String, Object> oTemplate,
			RedisTemplate<String, byte[]> bTemplate) {

		var limiter = new RedisTokenBucket(clock, oTemplate);
		limiter.setRate(properties.getEffective().getRate());
		limiter.setBucketSize(properties.getEffective().getBucketSize());
		return new EffectRateLimitFilter(limiter, bTemplate, threadPool);
	}
}
