package net.kaciras.blog.facade.filter;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.defense.DefenseService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;

@RequiredArgsConstructor
@Component
public class DefenseInterceptor extends HandlerInterceptorAdapter {

	private final DefenseService defenseService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			return true;
		}
		boolean allow = defenseService.accept(InetAddress.getByName(request.getRemoteAddr()));
		if (!allow) {
			response.setStatus(429);
		}
		return allow;
	}
}
