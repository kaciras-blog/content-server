package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.principal.AuthorizeAspect;
import com.kaciras.blog.infra.principal.ServletPrincipalFilter;
import com.kaciras.blog.infra.principal.ServletSecurityContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

final class KxPrincipalAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(KxPrincipalAutoConfiguration.class));

	// 没啥好测的，就测一下启动算了
	@Test
	void defaults () {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ServletPrincipalFilter.class);
			assertThat(context).doesNotHaveBean(ServletSecurityContextFilter.class);
			assertThat(context).hasSingleBean(AuthorizeAspect.class);
		});
	}

	@Test
	void enableSecurityContext() {
		contextRunner.withPropertyValues("app.authorization.security-context=true")
				.run(context -> assertThat(context).hasSingleBean(ServletSecurityContextFilter.class));
	}
}
