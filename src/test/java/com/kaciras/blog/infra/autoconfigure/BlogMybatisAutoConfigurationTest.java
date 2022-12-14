package com.kaciras.blog.infra.autoconfigure;

import com.google.common.reflect.ClassPath;
import com.kaciras.blog.infra.MybatisMapperAspect;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class BlogMybatisAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(BlogMybatisAutoConfiguration.class);

	/**
	 * 获取一个包内所有指定类的子类，不包括指定的类本身。
	 *
	 * @param clazz 指定的类
	 * @param pkg   包名
	 * @return 类列表，没有泛型因为调用方可能要转换
	 */
	@SuppressWarnings({"rawtypes"})
	public static <T> Stream getSubClassesInPackage(Class<T> clazz, String pkg) {
		try {
			return ClassPath
					.from(clazz.getClassLoader())
					.getTopLevelClasses(pkg)
					.stream()
					.map(ClassPath.ClassInfo::load)
					.filter(clazz::isAssignableFrom)
					.filter(c -> !c.equals(clazz));
		} catch (IOException e) {
			throw new Error("getSubClassesInPackage 方法有 BUG", e);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	void defaults() {
		var pkg = "com.kaciras.blog.infrastructure.codec";
		var shouldRegistered = (List)getSubClassesInPackage(TypeHandler.class, pkg)
				.collect(Collectors.toList());

		contextRunner.run(context -> {
			var customizer = context.getBean(ConfigurationCustomizer.class);

			var mybatisConfig = new Configuration();
			customizer.customize(mybatisConfig);

			var handlers = mybatisConfig.getTypeHandlerRegistry()
					.getTypeHandlers()
					.stream()
					.map(Object::getClass);

			assertThat(handlers).containsAll(shouldRegistered);
			assertThat(context).hasSingleBean(MybatisMapperAspect.class);
		});
	}
}
