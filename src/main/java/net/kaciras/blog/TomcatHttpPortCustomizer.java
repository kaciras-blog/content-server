package net.kaciras.blog;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "server.http-port")
@Component
final class TomcatHttpPortCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	@Value("${server.http-port}")
	private int port;

	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		var connector = new Connector();
		connector.setPort(port);
		factory.addAdditionalTomcatConnectors(connector);
	}
}
