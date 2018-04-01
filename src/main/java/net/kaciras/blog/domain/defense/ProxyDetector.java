package net.kaciras.blog.domain.defense;

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
final class ProxyDetector {

	private Cache<InetAddress, Boolean> cache;

	private boolean enable = false;

	@Autowired
	void setCacheManager(CacheManager cacheManager) {
		CacheConfigurationBuilder<InetAddress, Boolean> builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(InetAddress.class, Boolean.class, ResourcePoolsBuilder.heap(1024))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(1)));
		cache = cacheManager.createCache("proxyAddress", builder.build());
	}

	boolean isProxy(InetAddress address) {
		if(!enable) {
			return false;
		}
		Boolean result = cache.get(address);
		if(result != null) {
			return result;
		}
		boolean record = getRecord(address);
		cache.put(address, record);
		return record;
	}

	private boolean getRecord(InetAddress address) {
		return false;
	}
}
