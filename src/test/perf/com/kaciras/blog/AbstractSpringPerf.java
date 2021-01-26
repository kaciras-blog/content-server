package com.kaciras.blog;

import lombok.Getter;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;

/**
 * Spring 容器的性能测试辅助类，继承该类可以启用 @Autowired.
 *
 * <h2>为什么不用单测方案</h2>
 * 单测方案指 Google 前几项里的方法，比如
 * <a href="https://gist.github.com/msievers/ce80d343fc15c44bea6cbb741dde7e45">这个</a>
 * <p>
 * IDEA 已有插件可以单独运行一个测试方法，单测的集成优势就不存在了。
 * 测试类通常不会有继承逻辑，所以这里用父类来实现是没问题的，不会占坑。
 *
 * <h2>使用注意</h2>
 * 用 @ContextConfiguration 可以指定配置类。
 * 由于存在 Spring 容器字段，子类必须添加 @State 注解。
 */
public abstract class AbstractSpringPerf {

	@SuppressWarnings("JmhInspections")
	@Getter
	private ConfigurableApplicationContext context;

	/**
	 * 测试结束时关闭 Spring 容器，注意如果子类也存在 @TearDown 和 @Setup 时，
	 * JMH 都是先调用父类的再是子类，这不遵循先开后关的原则。
	 * <p>
	 * 所以如果子类需要在关闭方法里访问 Spring 容器需要重写此方法，并在最后调用 super。
	 *
	 * @throws Exception 子类重写可能需要抛异常
	 */
	@TearDown
	public void closeSpringContext() throws Exception {
		context.close();
	}

	@Setup
	public final void initSpringContext() {
		var sources = new Class<?>[0];

		var config = AnnotationUtils.findAnnotation(this.getClass(), ContextConfiguration.class);
		if (config != null) {
			sources = config.classes();
		}

		context = new SpringApplicationBuilder(sources)
				.profiles("test", "perf")
				.web(WebApplicationType.NONE)
				.run();
		context.getAutowireCapableBeanFactory().autowireBean(this);
	}
}
