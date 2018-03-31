package net.kaciras.blog.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration("ConfigurationContextConfig")
class ContextConfig {

	@Bean
	public List<Converter<?>> configConverters() {
		return Arrays.asList();
	}
}
