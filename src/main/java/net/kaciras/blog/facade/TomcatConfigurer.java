package net.kaciras.blog.facade;

import lombok.Setter;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tomcat")
@Setter
class TomcatConfigurer {

	private final String PROTOCOL_CLASS = "org.apache.coyote.http11.Http11Nio2Protocol";

	@Value("${server.port}")
	private int port;

	private String cert;
	private String pvk;
	private String chain;

	@Bean
	public TomcatServletWebServerFactory tomcatCustomizer() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.setProtocol(PROTOCOL_CLASS);
		if(cert != null) {
			factory.addConnectorCustomizers(this::enableHttp2);
		}
		return factory;
	}

	private void enableHttp2(Connector connector) {
		SSLHostConfig sc = new SSLHostConfig();
		sc.setCertificateFile(cert);
		sc.setCertificateChainFile(chain);
		sc.setCertificateKeyFile(pvk);
		connector.addSslHostConfig(sc);
		connector.setSecure(true);
		connector.setAttribute("sslImplementationName", "org.apache.tomcat.util.net.jsse.JSSEImplementation");
		connector.setAttribute("SSLEnabled", "true");
		connector.addUpgradeProtocol(new Http2Protocol());
	}
}
