package net.kaciras.blog.facade;

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
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response,
							 Object handler) throws Exception {
		if (HttpMethod.OPTIONS.matches(request.getMethod()) || defenseService.accept(request)) {
			return true;
		}
		response.setStatus(429);
		return false;
	}
}
