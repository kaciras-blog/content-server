package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import javax.validation.Validator;

@RequiredArgsConstructor
@Configuration
public class ConfigBindingAutoConfiguration {

	private final GenericApplicationContext context;

	@Bean
	public ConfigBindingManager configService(ConfigRepository configRepository,
											  @Autowired(required = false) Validator validator) {
		return new ConfigBindingManager(configRepository, validator);
	}

	@Bean
	public ConfigBindingPostProcessor bindingPostProcessor(ConfigBindingManager service) {
		return new ConfigBindingPostProcessor(context, service);
	}
}
