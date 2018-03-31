package net.kaciras.blog.facade.filter;

import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
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

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true; //OPTIONS请求不需要用户信息
		}
		HttpSession session = request.getSession(true);
		Object userId = session.getAttribute("UserId");
		if (userId != null) {
			SecurtyContext.setCurrentUser((Integer) userId);
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		SecurtyContext.setCurrentUser(null);
	}
}
