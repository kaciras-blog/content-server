package net.kaciras.blog.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DefaultEventLoopGroupProvider;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import net.kaciras.blog.infrastructure.Misc;
import net.kaciras.blog.infrastructure.autoconfig.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.task.TaskSchedulerCustomizer;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
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
	 * TaskSchedulingAutoConfiguration.taskScheduler 因为 RedisHttpSessionConfiguration 实现了
	 * SchedulingConfigurer 来加入一个清理过期会话的任务而不被启用。
	 * <p>
	 * 这是因为（我猜的）SchedulingConfigurer 功能过大可以修改 ScheduledTaskRegistrar 的调度器，
	 * 保守起见禁止了默认的调度器Bean。
	 * <p>
	 * 虽然本应用中的 RedisHttpSessionConfiguration 并不会修改调度器，但 TaskSchedulingAutoConfiguration
	 * 却是废了，故直接在这里创建调度器。
	 * <p>
	 * 与 TaskSchedulingAutoConfiguration 相比，这里创建不支持配置文件(spring.task.scheduling)，如果要用回自动
	 * 配置的话，需要将 application.yml 中 spring.autoconfigure.exclude 相关项去掉
	 *
	 * @see org.springframework.scheduling.config.ScheduledTaskRegistrar
	 * @see RedisHttpSessionConfiguration#configureTasks
	 * @see org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
	 */
	@Bean
	ThreadPoolTaskScheduler taskScheduler(ObjectProvider<TaskSchedulerCustomizer> customizers) {
		var taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setThreadNamePrefix("SharedThreadPool-");
		customizers.forEach(customizer -> customizer.customize(taskScheduler));

		return taskScheduler;
	}

	/**
	 * 减少Lettuce的线程数，注意这里降低到了比推荐的最低数量还少，可能会造成性能问题。
	 * EventLoopGroup 和 EventExecutorGroup 的类型与 DefaultClientResources 构造方法里的默认类型一致。
	 *
	 * @see org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration#lettuceClientResources()
	 */
	@Bean(destroyMethod = "shutdown")
	public DefaultClientResources lettuceClientResources() {
		return DefaultClientResources.builder()
				.eventExecutorGroup(DefaultEventLoopGroupProvider.createEventLoopGroup(DefaultEventExecutorGroup.class, 2))
				.eventLoopGroupProvider(new DefaultEventLoopGroupProvider(1)).build();
	}

	/**
	 * 使用 JAVA8 的新 API 代替 System.currentTimeMillis()，Clock 具有更好的语义并且便于Mock测试。
	 */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

	@Profile("!dev")
	@Bean
	HttpClient httpClient(ThreadPoolTaskScheduler threadPool) {
		return HttpClient.newBuilder().executor(threadPool).build();
	}

	@Profile("dev")
	@Bean("httpClient")
	HttpClient devHttpClient(ThreadPoolTaskScheduler threadPool) {
		return HttpClient.newBuilder()
				.executor(threadPool)
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
