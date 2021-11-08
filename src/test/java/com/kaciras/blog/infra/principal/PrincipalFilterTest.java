package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.FilterChainCapture;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

final class PrincipalFilterTest {

	// 【更新】增加不创建 Session 的断言
	@Test
	void noSession() throws Exception {
		var filter = new PrincipalFilter(false);

		var result = FilterChainCapture.doFilter(filter, new MockHttpServletRequest());

		assertThat(result.outRequest.getSession(false)).isNull();
		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void noLogin() throws Exception {
		var filter = new PrincipalFilter(false);
		var request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());

		var result = FilterChainCapture.doFilter(filter, request);

		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void logined() throws Exception {
		var filter = new PrincipalFilter(false);
		var request = new MockHttpServletRequest();

		var session = new MockHttpSession();
		session.setAttribute("UserId", 666);
		request.setSession(session);

		var result = FilterChainCapture.doFilter(filter, request);

		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.getId()).isEqualTo(666);
	}

	@Test
	void debugAdmin() throws Exception {
		var filter = new PrincipalFilter(true);

		var result = FilterChainCapture.doFilter(filter, new MockHttpServletRequest());

		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.isAdminister()).isTrue();
	}
}
