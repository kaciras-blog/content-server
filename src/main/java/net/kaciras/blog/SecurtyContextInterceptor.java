package net.kaciras.blog;

import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 在调用处理请求的方法前，将存储在会话中的用户加入到SecurtyContext，
 * 并在调用完成后清除。
 */
@Component
public class SecurtyContextInterceptor extends HandlerInterceptorAdapter {

	private static final String SESSION_NAME = "CSRF-Token";
	private static final String HEADER_NAME = "X-CSRF-Token";

	@Value("${web.csrf-verify}")
	private boolean csrfVerify;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod))
			return true;
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true; //OPTIONS请求不需要用户信息
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
		//在配置文件里可以关闭CSRF检验
		if (!csrfVerify) {
			return true;
		}
		var csrf = request.getSession().getAttribute(SESSION_NAME);
		var header = request.getHeader(HEADER_NAME);
		return csrf != null && csrf.equals(header);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		SecurtyContext.setCurrentUser(null);
	}
}
