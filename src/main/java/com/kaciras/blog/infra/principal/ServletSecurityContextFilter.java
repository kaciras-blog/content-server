package com.kaciras.blog.infra.principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 把请求里的 Principle 对象加入到 SecurityContext 全局类，在请求结束后自动清理。
 * 当 Web 程序需要依靠 SecurityContext 来鉴权时使用。
 */
public final class ServletSecurityContextFilter extends HttpFilter {

	@Override
	protected void doFilter(HttpServletRequest request,
							HttpServletResponse response,
							FilterChain chain)
			throws IOException, ServletException {

		SecurityContext.setPrincipal((WebPrincipal) request.getUserPrincipal());
		chain.doFilter(request, response);
		SecurityContext.setPrincipal(null);
	}
}
