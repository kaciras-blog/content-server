package net.kaciras.blog.api.perm;

import net.kaciras.blog.api.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Principal;

@Component
public final class PrincipalFilter extends HttpFilter {

	private static final String SESSION_NAME = "CSRF-Token";
	private static final String HEADER_NAME = "X-CSRF-Token";

	@Value("${web.csrf-verify}")
	private boolean csrfVerify;

	@Value("${debug-permission}")
	private boolean debugPermission;

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			chain.doFilter(request, response); // OPTIONS请求不需要用户信息
		} else {
			chain.doFilter(new PrincipalRequestWrapper(request), response);
		}
	}

	private class PrincipalRequestWrapper extends HttpServletRequestWrapper {

		public PrincipalRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public Principal getUserPrincipal() {
			var session = super.getSession();
			var address = Utils.getAddress(super.getRemoteAddr());
			Object userId;

			if (debugPermission) {
				return new WebPrincipal(WebPrincipal.ADMIN_ID, InetAddress.getLoopbackAddress());
			}
			if (session == null || (userId = session.getAttribute("UserId")) == null || !checkCSRF()) {
				return new WebPrincipal(WebPrincipal.ANYNOMOUS_ID, address);
			}
			// system principal?
			return new WebPrincipal((Integer) userId, address);
		}

		private boolean checkCSRF() {
			if (!csrfVerify) {
				return true; //在配置文件里可以关闭CSRF检验
			}
			var csrf = getSession().getAttribute(SESSION_NAME);
			return csrf != null && csrf.equals(getHeader(HEADER_NAME));
		}
	}
}
