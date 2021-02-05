package com.kaciras.blog.api.ratelimit;

import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.infra.ratelimit.RedisBlockingLimiter;
import com.kaciras.blog.infra.ratelimit.RedisTokenBucket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Clock;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterConfiguration {

	private final RateLimiterProperties properties;

	private final RedisConnectionFactory factory;
	private final Clock clock;

	private final RedisTemplate<String, Object> redis;

	public RateLimiterConfiguration(RateLimiterProperties properties, RedisConnectionFactory factory, Clock clock) {
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

	// 注意这些 @Bean 方法是有前后顺序的，不要乱改。

	@ConditionalOnProperty(prefix = "app.rate-limiter.generic", name = {"rate", "size"})
	@Bean
	RateLimitChecker genericRateChecker() {
		var bucket = properties.generic;
		var limiter = new RedisTokenBucket(RedisKeys.RateLimit.value(), redis, clock);
		limiter.addBucket(bucket.size, bucket.rate);

		return (ip, request) -> limiter.acquire(ip.toString(), 1);
	}

	@ConditionalOnProperty(prefix = "app.rate-limiter.effective", name = "block-times")
	@Bean
	EffectRateChecker effectRateChecker() {
		var config = properties.effective;
		var inner = new RedisTokenBucket(RedisKeys.EffectRate.value(), redis, clock);

		for (var limit : config.limits) {
			var rate = limit.permits / (double) limit.time.toSeconds();
			inner.addBucket(limit.permits, rate);
		}

		var wrapper = new RedisBlockingLimiter(RedisKeys.EffectBlocking.value(), inner, factory, clock);
		wrapper.setBlockTimes(config.blockTimes);
		wrapper.setRefreshOnReject(config.refreshOnReject);

		return new EffectRateChecker(wrapper);
	}

	// ConditionalOnBean 需要指定的 bean 先注册，所以这个必须放到最下面
	@ConditionalOnBean(value = RateLimitChecker.class)
	@Bean
	RateLimitFilter rateLimitFilter(List<RateLimitChecker> checkers) {
		return new RateLimitFilter(checkers);
	}
}
