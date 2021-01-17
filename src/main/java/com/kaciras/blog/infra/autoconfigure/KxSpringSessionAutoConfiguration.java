package com.kaciras.blog.infra.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session 对 Cookie 的序列化没有自动配置支持，所以写了这个类来自动设置。
 * <p>
 * 【注意1】因为 SessionAutoConfiguration 不支持 Webflux 以及 setSameSite 属性所以才搞了这个类。
 * 【注意2】这个类可能会被 Spring 的自动配置取代，所以我懒得测试了
 */
@EnableConfigurationProperties(SessionCookieProperties.class)
@ConditionalOnClass(Session.class)
@Configuration(proxyBeanMethods = false)
public class KxSpringSessionAutoConfiguration {

	@ConditionalOnMissingBean(CookieSerializer.class)
	@Bean
	public CookieSerializer cookieSerializer(SessionCookieProperties options) {
		var serializer = new DefaultCookieSerializer();
		serializer.setCookieName(options.getName());
		serializer.setDomainName(options.getDomain());
		serializer.setSameSite(options.getSameSite());
		serializer.setCookieMaxAge(options.getMaxAge());
		serializer.setUseSecureCookie(options.isSecure());

		// SpringSession默认使用UUID.toString()，没必要Base64，而且保持跟Webflux的一致.
		serializer.setUseBase64Encoding(false);
		return serializer;
	}
}
