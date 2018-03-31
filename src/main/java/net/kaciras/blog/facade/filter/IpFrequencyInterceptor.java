package net.kaciras.blog.facade.filter;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.facade.AccessFrequencyService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class IpFrequencyInterceptor extends HandlerInterceptorAdapter {

	private final AccessFrequencyService controlService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		boolean allow = controlService.isAllow(request);
		if (!allow) {
			response.setStatus(429);
		}
		return allow;
	}
}
