package com.kaciras.blog.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.autoconfigure.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

/**
 * 【注意】在配置文件里排除了一些配置，添加新功能时记得看下有没有需要的依赖被排除了。
 */
@EnableScheduling
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Import({
		KxGlobalCorsAutoConfiguration.class,
		KxWebUtilsAutoConfiguration.class,
		KxSpringSessionAutoConfiguration.class,
		KxCodecAutoConfiguration.class,
		KxPrincipalAutoConfiguration.class,
		DevelopmentAutoConfiguration.class,
		HttpClientAutoConfiguration.class,
})
/*
 * proxyBeanMethods 的作用是代理配置类中标记了@Bean的方法，使其在内部调用时也能
 * 返回同一个单例（如果是单例bean），而不是在执行真正的方法创建一个。
 *
 * 由于我以前试的时候出了点问题，所以从来不在内部调用@Bean方法，依赖都通过参数获取，刚好避开这种用法。
 * 所以就可以把 proxyBeanMethods 设为 false 省掉代理的消耗。
 */
@SpringBootApplication(proxyBeanMethods = false)
public class ServiceApplication {

	/**
	 * EnableLoadTimeWeaving 中注册的 LoadTimeWeaver 可能在其他的 Bean 之后创建，这导致了
	 * 在其创建之前加载的类无法织入，所以将其加入构造方法以保证其创建时间先于 ComponentScan 扫描到的类
	 */
	@SuppressWarnings("unused")
	ServiceApplication(LoadTimeWeaver loadTimeWeaver) {}

	@Bean
	RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory factory) {
		var template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(factory);
		template.setDefaultSerializer(RedisSerializer.json());
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
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
	 * 我猜是因为 SchedulingConfigurer 功能过大可以修改 ScheduledTaskRegistrar 的调度器，
	 * 保守起见禁止了默认的调度器Bean。
	 * <p>
	 * 虽然本应用中的 RedisHttpSessionConfiguration 并不会修改调度器，但 TaskSchedulingAutoConfiguration
	 * 却是废了，故直接在这里创建调度器。
	 * <p>
	 * 与 TaskSchedulingAutoConfiguration 相比，这里创建不支持配置文件(spring.task.scheduling)，如果要用回自动
	 * 配置的话，需要将 application.yml 中 spring.autoconfigure.exclude 相关项去掉
	 *
	 * @see org.springframework.scheduling.config.ScheduledTaskRegistrar
	 * @see RedisHttpSessionConfiguration.SessionCleanupConfiguration#configureTasks
	 * @see org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
	 */
	@Bean
	ThreadPoolTaskScheduler taskScheduler(ObjectProvider<TaskSchedulerCustomizer> customizers) {
		var taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(2);
		taskScheduler.setThreadNamePrefix("SharedPool-");
		customizers.forEach(customizer -> customizer.customize(taskScheduler));
		return taskScheduler;
	}

	/**
	 * 使用 JAVA8 的新 API 代替 System.currentTimeMillis()，Clock 具有更好的语义并且便于 Mock 测试。
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	public static void main(String... args) {
		Misc.disableIllegalAccessWarning();
		new SpringApplicationBuilder(ServiceApplication.class).listeners(new ApplicationPidFileWriter()).run(args);
	}
}
