package com.kaciras.blog.infra.principal;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

/**
 * <h2>关于 CSRF 的问题</h2>
 * 现代的浏览器都支持 SameSite Cookie，无需再自己设计 CSRF token 机制。
 * 不支持 SameSite 一律视为自己的浏览器不安全，本项目不管。
 */
@Order(10_000)
@Slf4j
@RequiredArgsConstructor
@Setter
public final class PrincipalFilter extends HttpFilter {

	private final boolean debugAdmin;

	@Override
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain
	) throws IOException, ServletException {
		chain.doFilter(new Wrapper(request), response);
	}

	private class Wrapper extends HttpServletRequestWrapper {

		private Wrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public Principal getUserPrincipal() {
			if (debugAdmin) {
				return new WebPrincipal(WebPrincipal.ADMIN_ID);
			}

			// 会话不一定存在，比如内部调用，如果没有就创建则会产生大量无用的会话
			// TODO: UserId 这一信息属于 infra 层吗？
			return Optional
					.ofNullable(getSession(false))
					.map(s -> s.getAttribute("UserId"))
					.map(id -> new WebPrincipal((Integer) id))
					.orElse(WebPrincipal.ANONYMOUS);
		}
	}
}
