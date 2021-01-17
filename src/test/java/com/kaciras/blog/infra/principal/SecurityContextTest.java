package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.exception.PermissionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class SecurityContextTest {

	@AfterEach
	void removePrincipal() {
		SecurityContext.setPrincipal(null);
	}

	@Test
	void securityContextFilter() throws Exception {
		var request = new MockHttpServletRequest();
		request.setUserPrincipal(new WebPrincipal(123));

		var filter = new ServletSecurityContextFilter();
		filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
			assertThat(SecurityContext.getUserId()).isEqualTo(123);
		});

		assertThat(SecurityContext.getPrincipal()).isNull();
	}

	@Test
	void checkPermission() {
		SecurityContext.setPrincipal(WebPrincipal.ANONYMOUS);
		assertThatThrownBy(() -> SecurityContext.require("TEST")).isInstanceOf(PermissionException.class);

		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ADMIN_ID));
		SecurityContext.require("TEST");
	}

	@Test
	void isNot() {
		SecurityContext.setPrincipal(new WebPrincipal(233));
		assertThat(SecurityContext.isNot(1)).isTrue();
		assertThat(SecurityContext.isNot(233)).isFalse();
	}

	@Test
	void requireId() {
		SecurityContext.setPrincipal(new WebPrincipal(233));
		SecurityContext.requireId(233);
		assertThatThrownBy(() -> SecurityContext.requireId(1)).isInstanceOf(PermissionException.class);
	}

	@Test
	void requireLogin() {
		SecurityContext.setPrincipal(WebPrincipal.ANONYMOUS);
		assertThatThrownBy(SecurityContext::requireLogin).isInstanceOf(PermissionException.class);

		SecurityContext.setPrincipal(new WebPrincipal(233));
		SecurityContext.requireLogin();
	}
}
