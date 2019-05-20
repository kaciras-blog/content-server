package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
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
	RateLimitFilter rateLimiterFilter(Clock clock, RedisTemplate<String, Object> template) {
		var limiter = new RedisRateLimiter(clock, template);
		limiter.setRate(properties.getRate());
		limiter.setBucketSize(properties.getBucketSize());
		return new RateLimitFilter(limiter);
	}

	@Bean
	EffectRateLimitFilter effectRateLimitFilter(
			Clock clock,
			RedisTemplate<String, Object> oTemplate,
			RedisTemplate<String, byte[]> bTemplate) {

		var limiter = new RedisRateLimiter(clock, oTemplate);
		limiter.setRate(0.5);
		limiter.setBucketSize(4);
		return new EffectRateLimitFilter(limiter, bTemplate);
	}
}
