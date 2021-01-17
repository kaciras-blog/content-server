package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.principal.AuthorizeAspect;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.ServletPrincipalFilter;
import com.kaciras.blog.infra.principal.ServletSecurityContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({AuthorizationProperties.class, SessionCookieProperties.class})
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class KxPrincipalAutoConfiguration {

	private final AuthorizationProperties authProps;
	private final SessionCookieProperties sessionProps;

	@Bean
	public ServletPrincipalFilter servletPrincipalFilter() {
		var filter = new ServletPrincipalFilter(authProps.isAdminPrincipal());
		filter.setDomain(sessionProps.getDomain());

		var csrfConfig = authProps.getCsrf();
		filter.setSkipSafe(csrfConfig.isSkipSafe());
		filter.setDynamicToken(csrfConfig.isDynamicCookie());
		filter.setCookieName(csrfConfig.getCookieName());
		filter.setHeaderName(csrfConfig.getHeaderName());
		filter.setParameterName(csrfConfig.getParameterName());
		return filter;
	}

	@ConditionalOnProperty(name = "app.authorization.security-context", havingValue = "true")
	@Bean
	public ServletSecurityContextFilter securityContextFilter() {
		return new ServletSecurityContextFilter();
	}

	/**
	 * 注册AOP权限拦截器，可以对一些简单的权限进行拦截。
	 *
	 * @return 切面类
	 * @see AuthorizeAspect
	 * @see RequirePermission
	 */
	@Bean
	public AuthorizeAspect principalAspect() {
		return new AuthorizeAspect();
	}

	/*
	 * 【更新】移除Domain接口，当初的设想是通过AOP自动切换领域，但是方法的调用错综复杂，而且
	 * 目前用不到纯属累赘。考虑到代码也没多少，直接删了完事。
	 */
}
