package net.kaciras.blog.facade.filter;

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

	private static final String ATTR_NAME = "CSRF-Token";

	@Value("${web.csrfVerify}")
	private boolean csrfVerify;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod))
			return true;
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true; //OPTIONS请求不需要用户信息
		}
		HttpSession session = request.getSession(true);
		Object userId = session.getAttribute("UserId");
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
		Object csrf = request.getSession().getAttribute(ATTR_NAME);
		String header = request.getHeader("X-CSRF-Token");
		return csrf != null && csrf.equals(header);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		SecurtyContext.setCurrentUser(null);
	}
}
