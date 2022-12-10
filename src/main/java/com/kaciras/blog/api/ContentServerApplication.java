package com.kaciras.blog.api;

import com.kaciras.blog.api.account.SessionService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

/**
 * 应用的启动入口，也包括一些基本组件的创建代码。
 *
 * <h2>可能出现的问题</h2>
 * 在配置文件里排除了一些配置，添加新功能时记得看下有没有需要的依赖被排除了。
 *
 * <h2>关于 proxyBeanMethods</h2>
 * proxyBeanMethods 的作用是代理配置类中标记了@Bean的方法，使其在内部调用时也能
 * 返回同一个单例（如果是单例bean），而不是在执行真正的方法创建一个。
 * <p>
 * 由于我以前试的时候出了点问题，所以从来不在内部调用 @Bean 方法，刚好避开这种用法。
 * 所以就可以把 proxyBeanMethods 设为 false 省掉代理的消耗。
 */
@EnableScheduling
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@SpringBootApplication(proxyBeanMethods = false)
public class ContentServerApplication {

	/**
	 * EnableLoadTimeWeaving 中注册的 LoadTimeWeaver 可能在其他的 Bean 之后创建，这导致了
	 * 在其创建之前加载的类无法织入，所以将其加入构造方法以保证其创建时间先于 ComponentScan 扫描到的类
	 */
	@SuppressWarnings("unused")
	ContentServerApplication(LoadTimeWeaver loadTimeWeaver) {}

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
	 * 使用 JAVA8 的新 API 代替 System.currentTimeMillis()。
	 *
	 * <h2>为什么不在 Mariadb 里生成时间</h2>
	 * 在测试中 Clock 能够 Mock，若使用真实数据库则 Clock 更好。
	 * <p>
	 * 并非所有数据库都能设置时间，Redis 就不行，所以应用里必须要用 Clock，
	 * 如果数据库里也设置则存在两个时间源，增加系统的复杂度。
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	/**
	 * 优化 Session Cookie 存储，因为值已经是 UUID 所以无需再 base64。
	 * 启用保持登录功能，本项目没用 Spring Security 所以要手动设置下。
	 */
	@Bean
	DefaultCookieSerializerCustomizer customCookie() {
		return serializer -> {
			serializer.setUseBase64Encoding(false);
			serializer.setRememberMeRequestAttribute(SessionService.REMEMBER_ME_ATTR);
		};
	}

	public static void main(String... args) {
		new SpringApplicationBuilder(ContentServerApplication.class).run(args);
	}
}
