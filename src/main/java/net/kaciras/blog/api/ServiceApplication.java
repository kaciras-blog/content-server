package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.AddontionPortAutoConfiguration;
import net.kaciras.blog.infrastructure.codec.KxCodecConfiguration;
import net.kaciras.blog.infrastructure.io.CommandListener;
import net.kaciras.blog.infrastructure.message.DirectCalledMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Import({KxCodecConfiguration.class, AddontionPortAutoConfiguration.class})
@SpringBootApplication
public class ServiceApplication {

	@SuppressWarnings("unused")
	ServiceApplication(LoadTimeWeaver loadTimeWeaver) {}

	/**
	 * EnableScheduling 注解将自动使用 TaskScheduler 类型的bean。
	 *
	 * @return TaskScheduler
	 */
	@Bean(destroyMethod = "destroy")
	ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		taskScheduler.setDaemon(true);
		taskScheduler.setThreadNamePrefix("Shed-");
		return taskScheduler;
	}

	@Bean
	MessageClient messageClient() {
		return new DirectCalledMessageClient();
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
		ConfigurableApplicationContext context = SpringApplication.run(ServiceApplication.class, args);
		CommandListener listener = new CommandListener(60002);
		listener.onShutdown(() -> SpringApplication.exit(context, () -> 0));
		listener.start();
	}
}
