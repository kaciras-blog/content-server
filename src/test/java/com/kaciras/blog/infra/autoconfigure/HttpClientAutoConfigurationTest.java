package com.kaciras.blog.infra.autoconfigure;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientAutoConfigurationTest {

	private final Executor executor = Mockito.mock(Executor.class);

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withBean("threadPool", Executor.class, () -> executor)
			.withUserConfiguration(HttpClientAutoConfiguration.class);

	@Test
	void proxy() {
		runner
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
		runner.withPropertyValues("app.http-client.executor=threadPool").run(context ->{
			var client = context.getBean(HttpClient.class);
			assertThat(client.executor()).get().isEqualTo(executor);
		});

		// 没法很直观地把属性设置为 null，只能用空串代替
		runner.withPropertyValues("app.http-client.executor=").run(context ->{
			var client = context.getBean(HttpClient.class);
			assertThat(client.executor()).isEmpty();
		});

		runner.withPropertyValues("app.http-client.executor=invalid").run(context -> assertThat(context).hasFailed());
	}

	@Test
	void timeout() {
		runner.withPropertyValues("app.http-client.timeout=8s").run(context -> {
			var client = context.getBean(HttpClient.class);
			assertThat(client.connectTimeout()).get().isEqualTo(Duration.ofSeconds(8));
		});
	}
}
