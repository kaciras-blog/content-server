package com.kaciras.blog.api;

import com.kaciras.blog.api.account.SessionService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Random;

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
	 * 同样为了 Mock 测试，以及少创建点对象，随机数生成器也由容器注入。
	 *
	 * <h2>性能</h2>
	 * ThreadLocalRandom 都是静态方法，不能注入，本站访问量不大就懒得管了。
	 */
	@Primary
	@Bean
	Random random() {
		return new Random();
	}

	@Bean
	SecureRandom secureRandom() {
		return new SecureRandom();
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
