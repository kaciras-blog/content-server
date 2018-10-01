package net.kaciras.blog.api;

import net.kaciras.blog.api.perm.PrincipalAspect;
import net.kaciras.blog.infrastructure.AddontionPortAutoConfiguration;
import net.kaciras.blog.infrastructure.codec.KxCodecConfiguration;
import net.kaciras.blog.infrastructure.exception.ExceptionResloverAutoConfiguration;
import net.kaciras.blog.infrastructure.io.CommandListener;
import net.kaciras.blog.infrastructure.message.DirectMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * 在配置文件里排除了一些配置，添加新功能时记得看下有没有需要的依赖被排除了。
 */
@EnableScheduling
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({
		ExceptionResloverAutoConfiguration.class,
		KxCodecConfiguration.class,
		AddontionPortAutoConfiguration.class,
})
@SpringBootApplication
public class ServiceApplication {

	@SuppressWarnings("unused")
	ServiceApplication(LoadTimeWeaver loadTimeWeaver) {}

	@Bean
	PrincipalAspect principalAspect() {
		return new PrincipalAspect();
	}

	@Bean
	MessageClient messageClient() {
		return new DirectMessageClient();
	}

	@Bean
	CacheManager cacheManager() {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		return template;
	}

	public static void main(String[] args) throws IOException {
		var context = SpringApplication.run(ServiceApplication.class, args);
		var listener = new CommandListener(60002);
		listener.onShutdown(() -> SpringApplication.exit(context, () -> 0));
		listener.start();
	}
}
