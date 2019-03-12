package net.kaciras.blog.api;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Clock;

@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration
@RequiredArgsConstructor
public class RateLimiterAutoConfiguration {

	private final RateLimiterProperties properties;

	@Bean
	RateLimiterFilter rateLimiterFilter(Clock clock, RedisConnectionFactory factory) {
		var limiter = new RedisRateLimiter(clock, factory);
		limiter.setRate(properties.getRate());
		limiter.setBucketSize(properties.getBucketSize());
		limiter.setCacheTime(properties.getCacheTime());
		return new RateLimiterFilter(limiter);
	}
}
