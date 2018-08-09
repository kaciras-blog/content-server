package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration("UserContextConfig")
class ContextConfig implements InitializingBean {

	private final BanRecordDao banRecordDao;

	public void afterPropertiesSet() {
		User.banRecordDao = banRecordDao;
	}

	@Bean("UserAuthenticator")
	Authenticator authenticator(AuthenticatorFactory factory) {
		return factory.create("USER");
	}
}
