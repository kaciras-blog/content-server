package net.kaciras.blog.api;

import net.kaciras.blog.api.perm.WebPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在调用处理请求的方法前，将存储在会话中的用户加入到SecurtyContext，
 */
@Component
class SecurtyContextInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		SecurityContext.setPrincipal((WebPrincipal) request.getUserPrincipal());
		return true;
	}
}
