package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.MybatisMapperAspect;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.stream.Collectors;

import static com.kaciras.blog.infra.TestHelper.getSubClassesInPackage;
import static org.assertj.core.api.Assertions.assertThat;

final class BlogMybatisAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(BlogMybatisAutoConfiguration.class);

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
