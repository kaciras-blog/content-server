package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("UserContextConfig")
class ContextConfig {

	private final LoginRecordDao loginRecordDao;

	@PostConstruct
	private void init() {
		User.loginRecordDao = loginRecordDao;
	}

	@Bean
	public Cache loginRecordCache(CacheManager cacheManager) {
		CacheConfigurationBuilder<Integer, LoginRecord> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
				Integer.class, LoginRecord.class, ResourcePoolsBuilder.heap(100));
		return User.cache = cacheManager.createCache("loginRecordCache", builder.build());
	}
}
