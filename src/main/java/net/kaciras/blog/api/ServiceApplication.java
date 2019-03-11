package net.kaciras.blog.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.kaciras.blog.infrastructure.Misc;
import net.kaciras.blog.infrastructure.autoconfig.*;
import net.kaciras.blog.infrastructure.message.DirectMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Clock;

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

	@Bean
	MessageClient messageClient() {
		return new DirectMessageClient();
	}

	@Bean
	RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, byte[]>();
		template.setEnableDefaultSerializer(false);
		template.setConnectionFactory(factory);
		template.setKeySerializer(RedisSerializer.string());
		return template;
	}

	@Bean
	RedisTemplate<byte[], byte[]> bytesRedisTemplate(RedisConnectionFactory connectionFactory) {
		var template = new RedisTemplate<byte[], byte[]>();
		template.setEnableDefaultSerializer(false);
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	/**
	 * 在序列化JSON时过滤掉为null的字段，可以减小一些体积并屏蔽不可见的字段。
	 * 该类型的Bean还有几个，为了防止重名给它起个App开头的名字。
	 */
	@Bean("AppJacksonCustomizer")
	Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
		return builder -> builder.serializationInclusion(Include.NON_NULL);
	}

	/**
	 * 使用 JAVA8 的新 API 代替 System.currentTimeMillis() 等旧API，Clock具有更好的语义并且便于Mock测试。
	 */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

	@Profile("!dev")
	@Bean
	HttpClient httpClient() {
		return HttpClient.newBuilder().build();
	}

	@Profile("dev")
	@Bean("httpClient")
	HttpClient devHttpClient() {
		return HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("localhost", 2080)))
				.build();
	}

	public static void main(String... args) throws Exception {
		Misc.disableHttpClientCertificateVerify();
		Misc.disableIllegalAccessWarning();
		Misc.disableSpringDevToolOnJarStartup();

		new SpringApplicationBuilder(ServiceApplication.class)
				.listeners(new ApplicationPidFileWriter()).run(args);
	}
}
