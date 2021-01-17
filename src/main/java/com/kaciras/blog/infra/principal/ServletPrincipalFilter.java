package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.Misc;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Order(10_000)
@Slf4j
@RequiredArgsConstructor
@Setter
public final class ServletPrincipalFilter extends HttpFilter {

	private final boolean debugAdmin;

	private String domain;
	private boolean dynamicToken;

	private boolean skipSafe;

	private String cookieName;
	private String headerName;
	private String parameterName;

	@Override
	protected void doFilter(HttpServletRequest rawRequest,
							HttpServletResponse response,
							FilterChain chain) throws IOException, ServletException {

		var request = new PrincipalRequestWrapper(rawRequest);
		chain.doFilter(request, response);

		// TODO: 用户登录后的初始Token是否也能搞到这里
		if (dynamicToken) {
			Optional.ofNullable(request.getSession(false))
					.map(session -> session.getAttribute("UserId"))
					.filter(__ -> !Misc.isSafeRequest(request))
					.ifPresent(__ -> changeCsrfCookie(request, response));
		}
	}

	private void changeCsrfCookie(HttpServletRequest request, HttpServletResponse response) {
		var cookie = new Cookie(cookieName, UUID.randomUUID().toString());
		cookie.setDomain(domain);
		cookie.setPath("/");
		cookie.setMaxAge(request.getSession().getMaxInactiveInterval());
		response.addCookie(cookie);
	}

	private class PrincipalRequestWrapper extends HttpServletRequestWrapper {

		private PrincipalRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public Principal getUserPrincipal() {
			if (debugAdmin) {
				return new WebPrincipal(WebPrincipal.ADMIN_ID);
			}

			// 会话不一定存在，比如内部调用，如果没有就创建则会产生大量无用的会话，故create参数设为false
			var userId = Optional.ofNullable(getSession(false))
					.map(session -> session.getAttribute("UserId"));

			if (!(skipSafe && Misc.isSafeRequest(this))) {
				userId = userId.filter((__) -> checkCSRF());
			}

			return userId.map((id) -> new WebPrincipal((Integer) id)).orElse(WebPrincipal.ANONYMOUS);
		}

		private boolean checkCSRF() {
			if (cookieName == null) {
				return true;
			}
			var nullable = Optional
					.ofNullable(WebUtils.getCookie(this, cookieName))
					.map(Cookie::getValue);

			if (headerName != null) {
				nullable = nullable.filter(token -> token.equals(getHeader(headerName)));
			}
			if (parameterName != null) {
				nullable = nullable.filter(token -> token.equals(getParameter(parameterName)));
			}
			return nullable.isPresent();
		}
	}
}
