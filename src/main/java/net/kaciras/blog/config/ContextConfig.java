package net.kaciras.blog.config;

import net.kaciras.blog.Authenticator;
import net.kaciras.blog.AuthenticatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("ConfigContextConfig")
public class ContextConfig {

	@Bean
	Authenticator configAuthenticator(AuthenticatorFactory factory) {
		return factory.create("CONFIG");
	}

}
