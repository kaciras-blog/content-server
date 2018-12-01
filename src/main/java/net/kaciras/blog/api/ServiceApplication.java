package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.Misc;
import net.kaciras.blog.infrastructure.autoconfig.*;
import net.kaciras.blog.infrastructure.message.DirectMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * 在配置文件里排除了一些配置，添加新功能时记得看下有没有需要的依赖被排除了。
 */
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Import({
		KxGlobalCorsAutoConfiguration.class,
		KxWebUtilsAutoConfiguration.class,
		KxSpringSessionAutoConfiguration.class,
		KxCodecAutoConfiguration.class,
		KxPrincipalAutoConfiguration.class,
		DevelopmentAutoConfiguration.class
})
@SpringBootApplication
public class ServiceApplication {

	@SuppressWarnings("unused")
	ServiceApplication(LoadTimeWeaver loadTimeWeaver) {}

	@ConditionalOnMissingBean
	@Bean
	MessageClient messageClient() {
		return new DirectMessageClient();
	}

	@ConditionalOnMissingBean
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@ConditionalOnMissingBean
	@Bean
	RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, byte[]>();
		template.setEnableDefaultSerializer(false);
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		return template;
	}

	public static void main(String... args) throws Exception {
		Misc.disableIllegalAccessWarning();
		Misc.disableURLConnectionCertVerify();

		new SpringApplicationBuilder(ServiceApplication.class)
				.listeners(new ApplicationPidFileWriter()).run(args);
	}
}
