package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.FilterChainCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;

final class ServletPrincipalFilterTest {

	private static final String COOKIE_NAME = "CSRF-Token";
	private static final String HEADER_NAME = "X-CSRF-Token";
	private static final String PARAMETER_NAME = "csrf";

	private final MockHttpServletRequest request = new MockHttpServletRequest();

	private ServletPrincipalFilter filter;

	/**
	 * 默认的请求和过滤器实例，大部分测试都是用它们。
	 * 请求带有Session，其中UserId=666，过滤器不开启debugAdmin。
	 */
	@BeforeEach
	void setUp() {
		filter = new ServletPrincipalFilter(false);
		var session = new MockHttpSession();
		request.setSession(session);
		session.setAttribute("UserId", 666);
	}

	// 【更新】增加不创建 Session 的断言
	@Test
	void noCredit() throws Exception {
		var result = FilterChainCapture.doFilter(filter, new MockHttpServletRequest());

		assertThat(result.outRequest.getSession(false)).isNull();
		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void disableCSRFCheck() throws Exception {
		filter.setCookieName(null);

		var result = FilterChainCapture.doFilter(filter, request);

		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.getId()).isEqualTo(666);
	}

	@Test
	void validHeader() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setHeaderName(HEADER_NAME);
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));
		request.addHeader(HEADER_NAME, "FOOBAR");

		var result = FilterChainCapture.doFilter(filter, request);

		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.getId()).isEqualTo(666);
	}

	@Test
	void invalidHeader() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setHeaderName(HEADER_NAME);
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));
		request.addHeader(HEADER_NAME, "invalid");

		var result = FilterChainCapture.doFilter(filter, request);
		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void validParameter() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setParameterName(PARAMETER_NAME);
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));
		request.addParameter(PARAMETER_NAME, "FOOBAR");

		var result = FilterChainCapture.doFilter(filter, request);

		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.getId()).isEqualTo(666);
	}

	@Test
	void invalidParameter() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setParameterName(PARAMETER_NAME);
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));

		var result = FilterChainCapture.doFilter(filter, request);
		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void dynamicToken() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setDomain("www.example.com");
		filter.setDynamicToken(true);

		request.setMethod("POST");
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));
		request.addHeader(HEADER_NAME, "FOOBAR");

		var result = FilterChainCapture.doFilter(filter, request);

		var cookie = MockCookie.parse(result.inResponse.getHeader("Set-Cookie"));
		assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
		assertThat(cookie.getDomain()).isEqualTo("www.example.com");
	}

	@Test
	void dynamicTokenSkipSafeRequest() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setDomain("www.example.com");
		filter.setDynamicToken(true);

		request.setMethod("GET");
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));
		request.addHeader(HEADER_NAME, "FOOBAR");

		var result = FilterChainCapture.doFilter(filter, request);
		assertThat(result.inResponse.getHeader("Set-Cookie")).isNull();
	}

	@Test
	void skipSafeRequest() throws Exception {
		filter.setCookieName(COOKIE_NAME);
		filter.setHeaderName(HEADER_NAME);
		filter.setSkipSafe(true);

		request.setMethod("GET");
		request.setCookies(new Cookie(COOKIE_NAME, "FOOBAR"));

		var result = FilterChainCapture.doFilter(filter, request);
		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.getId()).isEqualTo(666);

		request.setMethod("POST");
		result = FilterChainCapture.doFilter(filter, request);
		assertThat(result.outRequest.getUserPrincipal()).isEqualTo(WebPrincipal.ANONYMOUS);
	}

	@Test
	void debugAdmin() throws Exception {
		filter = new ServletPrincipalFilter(true);
		filter.setCookieName(COOKIE_NAME);

		var result = FilterChainCapture.doFilter(filter, new MockHttpServletRequest());
		var principal = (WebPrincipal) result.outRequest.getUserPrincipal();
		assertThat(principal.isAdminister()).isTrue();
	}
}
