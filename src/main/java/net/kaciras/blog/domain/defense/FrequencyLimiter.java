package net.kaciras.blog.domain.defense;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import net.kaciras.blog.domain.ConfigBind;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
final class FrequencyLimiter {

	private Cache<Integer, Counter> cache;

	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		CacheConfigurationBuilder<Integer, Counter> builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Integer.class, Counter.class, ResourcePoolsBuilder.heap(1024))
				.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)));
		cache = cacheManager.createCache("frequencyLimiter", builder.build());
	}

	@ConfigBind("defense.frequency.threshold")
	private int threshold;

	@ConfigBind("defense.frequency.enable")
	private boolean enable;

	boolean isAllow(InetAddress address, String operation) {
		if (!enable) {
			return true;
		}

		int hash = Objects.hash(address, operation);
		Counter counter = cache.get(hash);

		if (counter == null) {
			counter = new Counter();
			cache.put(hash, counter);
			return true;
		}

		/* 每当访问超过指定次数就刷新封禁时间，防止持续探测是否解封 */
		if(counter.difference() > threshold) {
			counter.mark();
			cache.put(hash, counter);
		}

		/* 为了少套几层代码，把incrementAndGet写后面了，不过影响不大，仅仅是刷新封禁时间的阈值会少一次 */
		return counter.incrementAndGet() < threshold;
	}

}
