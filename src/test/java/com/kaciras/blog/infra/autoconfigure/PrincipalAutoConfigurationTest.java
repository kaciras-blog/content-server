package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.principal.AuthorizeAspect;
import com.kaciras.blog.infra.principal.PrincipalFilter;
import com.kaciras.blog.infra.principal.SecurityContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

final class PrincipalAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withUserConfiguration(PrincipalAutoConfiguration.class);

	// 没啥好测的，就启动一下算了
	@Test
	void defaults() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(PrincipalFilter.class);
			assertThat(context).hasSingleBean(SecurityContextFilter.class);
			assertThat(context).hasSingleBean(AuthorizeAspect.class);
		});
	}
}
