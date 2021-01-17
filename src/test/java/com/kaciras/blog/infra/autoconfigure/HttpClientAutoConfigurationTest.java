package com.kaciras.blog.infra.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

	@Test
	void proxy() {
		contextRunner
				.withPropertyValues("app.http-client.proxy=localhost:1080")
				.run(context -> {
					var selector = context.getBean(HttpClient.class).proxy();
					assertThat(selector).isPresent();

					var proxy = selector.get().select(new URI("https://example.com"));
					var except = new InetSocketAddress("localhost", 1080);
					assertThat(proxy.get(0).address()).isEqualTo(except);
				});
	}

	@Test
	void executor() {
		var executor = Executors.newSingleThreadExecutor();
		var runner = contextRunner.withBean("threadPool", ExecutorService.class, () -> executor);

		runner.withPropertyValues("app.http-client.executor=threadPool").run(context ->
				assertThat(context.getBean(HttpClient.class).executor()).get().isEqualTo(executor));

		runner.withPropertyValues("app.http-client.executor=invalid").run(context -> assertThat(context).hasFailed());
	}

	@Test
	void timeout() {
		contextRunner.withPropertyValues("app.http-client.timeout=8s").run(context ->
				assertThat(context.getBean(HttpClient.class).connectTimeout()).get().isEqualTo(Duration.ofSeconds(8)));
	}
}
