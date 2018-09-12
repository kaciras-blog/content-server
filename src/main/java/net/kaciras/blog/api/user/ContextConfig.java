package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.AuthenticatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration("UserContextConfig")
class ContextConfig {

	@Bean("UserAuthenticator")
	Authenticator authenticator(AuthenticatorFactory factory) {
		return factory.create("USER");
	}
}
