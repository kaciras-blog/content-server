package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.ExceptionResolver;
import com.kaciras.blog.infra.func.UncheckedBiConsumer;
import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// 本测试会启动真实的Tomcat服务器而不是Mock
final class KxWebUtilsAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withPropertyValues("server.port=0")
			.withConfiguration(AutoConfigurations.of(
					KxWebUtilsAutoConfiguration.class,
					ServletWebServerFactoryAutoConfiguration.class));

	private static final class TestServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			resp.setStatus(200);
			resp.getWriter().write("Hello");
			resp.flushBuffer();
		}
	}

	private void runWithServer(WebApplicationContextRunner runner,
							   UncheckedBiConsumer<AssertableWebApplicationContext, Connector[]> test) {
		runner.run(context -> {
			var factory = context.getBean(TomcatServletWebServerFactory.class);
			var server = (TomcatWebServer) factory.getWebServer(ctx ->
					ctx.addServlet("test", new TestServlet()).addMapping("/"));

			server.getTomcat().setSilent(true);
			server.start();
			try {
				var connectors = server.getTomcat().getService().findConnectors();
				test.acceptThrows(context, connectors);
			} finally {
				server.stop();
			}
		});
	}

	private HttpResponse<String> request(String url) throws Exception {
		var request = HttpRequest
				.newBuilder(URI.create(url))
				.build();
		var response = HttpClient
				.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.body()).isEqualTo("Hello");
		assertThat(response.statusCode()).isEqualTo(200);

		return response;
	}

	@Test
	void defaults() {
		runWithServer(contextRunner, ((context, connectors) -> {
			assertThat(context).doesNotHaveBean("additionalConnectorCustomizer");
			assertThat(context).doesNotHaveBean("springH2CCustomizer");
			assertThat(context).hasSingleBean(ExceptionResolver.class);

			var resp = request("http://localhost:" + connectors[0].getLocalPort());
			assertThat(resp.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
		}));
	}

	@Test
	void springH2CCustomizer() {
		var runner = contextRunner.withPropertyValues("server.http2.enabled=true");
		runWithServer(runner, (context, connectors) -> {
			assertThat(context).hasBean("springH2CCustomizer");

			var resp = request("http://localhost:" + connectors[0].getLocalPort());
			assertThat(resp.version()).isEqualTo(HttpClient.Version.HTTP_2);
		});
	}

	@Test
	void additionalHttp11() {
		var runner = contextRunner.withPropertyValues("server.additional-connector.port=0");
		runWithServer(runner, (context, connectors) -> {
			assertThat(context).hasBean("additionalConnectorCustomizer");

			var resp = request("http://localhost:" + connectors[1].getLocalPort());
			assertThat(resp.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
		});
	}

	@Test
	void additionalHttp2() {
		var runner = contextRunner.withPropertyValues(
				"server.additional-connector.port=0",
				"server.http2.enabled=true"
		);
		runWithServer(runner, (__, connectors) -> {
			var resp = request("http://localhost:" + connectors[1].getLocalPort());
			assertThat(resp.version()).isEqualTo(HttpClient.Version.HTTP_2);
		});
	}
}
