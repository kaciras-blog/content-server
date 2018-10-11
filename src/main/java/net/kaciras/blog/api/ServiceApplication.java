package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.AddontionPortAutoConfiguration;
import net.kaciras.blog.infrastructure.DevelopmentAutoConfiguration;
import net.kaciras.blog.infrastructure.codec.KxCodecConfiguration;
import net.kaciras.blog.infrastructure.exception.ExceptionResloverAutoConfiguration;
import net.kaciras.blog.infrastructure.io.CommandListener;
import net.kaciras.blog.infrastructure.message.DirectMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.KxPrincipalAutoConfiguration;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * 在配置文件里排除了一些配置，添加新功能时记得看下有没有需要的依赖被排除了。
 */
@EnableScheduling
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Import({
		ExceptionResloverAutoConfiguration.class,
		KxCodecConfiguration.class,
		AddontionPortAutoConfiguration.class,
		KxPrincipalAutoConfiguration.class,
		DevelopmentAutoConfiguration.class
})
@SpringBootApplication
public class ServiceApplication {

	@SuppressWarnings("unused")
	ServiceApplication(LoadTimeWeaver loadTimeWeaver) {}

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

	@Bean
	CookieSerializer cookieSerializer(ServletContext servletContext) {
		var serializer = new DefaultCookieSerializer();
		serializer.setSameSite(null);
		serializer.setUseSecureCookie(false); // 默认跟连接的SSL一致
		serializer.setDomainName(servletContext.getSessionCookieConfig().getDomain());
		serializer.setCookieMaxAge(30 * 24 * 60 * 60);
		return serializer;
	}

	public static void main(String[] args) throws IOException {
		var context = SpringApplication.run(ServiceApplication.class, args);
		var listener = new CommandListener(60002);
		listener.onShutdown(() -> SpringApplication.exit(context, () -> 0));
		listener.start();
	}
}
