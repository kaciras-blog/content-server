package net.kaciras.blog.domain.config;

import net.kaciras.blog.domain.Authenticator;
import net.kaciras.blog.domain.AuthenticatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("ConfigContextConfig")
public class ContextConfig {

	@Bean
	Authenticator configAuthenticator(AuthenticatorFactory factory) {
		return factory.create("CONFIG");
	}

}
