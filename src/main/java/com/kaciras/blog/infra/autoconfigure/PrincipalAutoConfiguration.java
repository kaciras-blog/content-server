package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.principal.AuthorizeAspect;
import com.kaciras.blog.infra.principal.PrincipalFilter;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.SecurityContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置身份与权限功能，从 Session 中读取用户并检查 CSRF，设置 principal 和 SecurityContext，以及 AOP 权限拦截。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({AuthorizationProperties.class})
@RequiredArgsConstructor
public class PrincipalAutoConfiguration {

	private final AuthorizationProperties authProps;

	@Bean
	public PrincipalFilter servletPrincipalFilter() {
		return new PrincipalFilter(authProps.isAdminPrincipal());
	}

	/**
	 * 注册 AOP 权限拦截器，可以对一些简单的权限进行拦截。
	 *
	 * @return 切面类
	 * @see RequirePermission
	 */
	@Bean
	public AuthorizeAspect authorizeAspect() {
		return new AuthorizeAspect();
	}

	@Bean
	public SecurityContextFilter securityContextFilter() {
		return new SecurityContextFilter();
	}
}
