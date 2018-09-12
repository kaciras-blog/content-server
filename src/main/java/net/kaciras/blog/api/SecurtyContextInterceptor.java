package net.kaciras.blog.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在调用处理请求的方法前，将存储在会话中的用户加入到SecurtyContext，
 * 并在调用完成后清除。
 */
@Component
class SecurtyContextInterceptor extends HandlerInterceptorAdapter {

	private static final String SESSION_NAME = "CSRF-Token";
	private static final String HEADER_NAME = "X-CSRF-Token";

	@Value("${debug-permission}")
	private boolean debugPermission;

	@Value("${web.csrf-verify}")
	private boolean csrfVerify;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true; // OPTIONS请求不需要用户信息
		}
		if(debugPermission) {
			SecurtyContext.setCurrentUser(2);
		}
		var session = request.getSession(false);
		if(session == null) {
			return true;
		}
		var userId = session.getAttribute("UserId");
		if (userId != null && checkCSRF(request)) {
			SecurtyContext.setCurrentUser((Integer) userId);
		}
		return true;
	}

	private boolean checkCSRF(HttpServletRequest request) {
		if (!csrfVerify) {
			return true; //在配置文件里可以关闭CSRF检验
		}
		var csrf = request.getSession().getAttribute(SESSION_NAME);
		return csrf != null && csrf.equals(request.getHeader(HEADER_NAME));
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		SecurtyContext.setCurrentUser(null);
	}
}
