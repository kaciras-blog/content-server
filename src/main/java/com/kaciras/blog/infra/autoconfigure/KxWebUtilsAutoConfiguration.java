package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.ExceptionResolver;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
		DevelopmentProperties.class,
		ServerProperties.class,
		AdditionalConnectorProperties.class,
})
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class KxWebUtilsAutoConfiguration {

	private final DevelopmentProperties developmentProperties;
	private final ServerProperties serverProperties;
	private final AdditionalConnectorProperties additionalConnectorProperties;

	/**
	 * 使Http服务器支持双端口连接，例如同时监听80和443，额外的端口由选项server.http-port指定。
	 * 但这会导致多一个Connector，消耗更多的资源。
	 * <p>
	 * 【注意】Firefox 不准备支持 h2c 所以没法在浏览器上用 HTTP/2
	 */
	@ConditionalOnClass(TomcatServletWebServerFactory.class)
	@ConditionalOnProperty(name = "server.additional-connector.port")
	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> additionalConnectorCustomizer() {
		return factory -> {
			var connector = new Connector();
			connector.setPort(additionalConnectorProperties.getPort());

			var protocolHandler = (AbstractProtocol<?>) connector.getProtocolHandler();
			protocolHandler.setMaxThreads(serverProperties.getTomcat().getMaxThreads());
			protocolHandler.setAddress(additionalConnectorProperties.getAddress());

			if (serverProperties.getHttp2().isEnabled()) {
				connector.addUpgradeProtocol(new Http2Protocol());
			}
			factory.addAdditionalTomcatConnectors(connector);
		};
	}

	/**
	 * Spring Boot 本身竟然不支持配置H2C，只有当 server.ssl.enabled = true 时才会配置 Http2Protocol。
	 * 这里自定义一个配置器，让没有启用 SSL 的 Tomcat 也开启HTTP2。
	 */
	@ConditionalOnClass(TomcatServletWebServerFactory.class)
	@ConditionalOnProperty(name = "server.http2.enabled", havingValue = "true")
	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> springH2CCustomizer() {
		return factory -> {
			var ssl = serverProperties.getSsl();
			if (ssl == null || !ssl.isEnabled()) {
				factory.addConnectorCustomizers(connector -> connector.addUpgradeProtocol(new Http2Protocol()));
			}
		};
	}

	@Bean
	public ExceptionResolver exceptionResolver() {
		return new ExceptionResolver(developmentProperties.isDebugErrorMessage());
	}
}
