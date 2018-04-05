package net.kaciras.blog.domain.config;

import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration("ConfigContextConfig")
public class ContextConfig {

	@Bean
	Authenticator configAuthenticator(AuthenticatorFactory factory) {
		return factory.create("CONFIG");
	}

}
