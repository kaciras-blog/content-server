package net.kaciras.blog.api.config;

import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.AuthenticatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("ConfigContextConfig")
public class ContextConfig {

	@Bean
	Authenticator configAuthenticator(AuthenticatorFactory factory) {
		return factory.create("CONFIG");
	}

}
