package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.principal.AuthorizeAspect;
import com.kaciras.blog.infra.principal.ServletPrincipalFilter;
import com.kaciras.blog.infra.principal.ServletSecurityContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

final class KxPrincipalAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withUserConfiguration(KxPrincipalAutoConfiguration.class);

	// 没啥好测的，就启动一下算了
	@Test
	void defaults() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ServletPrincipalFilter.class);
			assertThat(context).hasSingleBean(ServletSecurityContextFilter.class);
			assertThat(context).hasSingleBean(AuthorizeAspect.class);
		});
	}
}
