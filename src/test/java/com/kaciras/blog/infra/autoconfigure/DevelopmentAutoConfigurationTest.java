package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.FilterChainCapture;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import javax.servlet.Filter;

import static org.assertj.core.api.Assertions.assertThat;

final class DevelopmentAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(DevelopmentAutoConfiguration.class));

	// 这时间波动也太大了，难道要用 PowerMockito 之类的来 mock Thread.sleep() ？
	@Test
	void delayFilter() {
		contextRunner.withPropertyValues("app.development.http-delay=200ms").run(context -> {
			var delayFilter = context.getBean(Filter.class);

			// warm up
			FilterChainCapture.doFilter((req, res, chain) -> {});

			// avoid gc pause in the next
			System.gc();

			var begin = System.currentTimeMillis();
			var capture = FilterChainCapture.doFilter(delayFilter);
			var end = System.currentTimeMillis();

			assertThat(end - begin).isCloseTo(200, Offset.offset(60L));
			assertThat(capture.outRequest).isNotNull();
		});
	}
}
