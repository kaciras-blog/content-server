package com.kaciras.blog.infra.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BlogJsonAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
			.withUserConfiguration(BlogJsonAutoConfiguration.class);

	@Test
	void defaults(){
		runner.run(context -> {
			assertThat(context).hasBean("jacksonCodecCustomizer");
			var objectMapper= context.getBean(ObjectMapper.class);
			var modules = objectMapper.getRegisteredModuleIds();
			assertThat(modules).contains("com.kaciras.blog.infra.codec.ExtendsCodecModule");
		});
	}
}
