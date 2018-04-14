package net.kaciras.blog.domain.defense;

import net.kaciras.blog.domain.ConfigBind;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;

@Component
final class FrequencyLimiter {

	private Cache<InetAddress, Integer> cache;

	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		CacheConfigurationBuilder<InetAddress, Integer> builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(InetAddress.class, Integer.class, ResourcePoolsBuilder.heap(1024))
				.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(30)));
		cache = cacheManager.createCache("frequencyLimiter", builder.build());
	}

	@ConfigBind("defense.frequency.threshold")
	private int threshold;

	@ConfigBind("defense.frequency.enable")
	private boolean enable;

	boolean isAllow(InetAddress address) {
		if (!enable) {
			return true;
		}
		Integer count = cache.get(address);
		if (count == null) {
			count = 0;
		} else if (count > threshold) {
			return false;
		}
		cache.put(address, ++count);
		return true;
	}
}
