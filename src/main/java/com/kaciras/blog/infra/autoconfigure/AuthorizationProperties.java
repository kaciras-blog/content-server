package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.authorization")
@Getter
@Setter
public final class AuthorizationProperties {

	/** 所有用户的身份都设为管理员，在调试时有用. */
	private boolean adminPrincipal;
}
