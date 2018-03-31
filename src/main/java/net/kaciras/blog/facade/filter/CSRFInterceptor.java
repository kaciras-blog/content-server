package net.kaciras.blog.facade.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class CSRFInterceptor extends HandlerInterceptorAdapter {

	private static final String ATTR_NAME = "CSRF-Token";
	private final String failMessage;

	@Value("${web.csrfVerify}")
	private boolean csrfVerify;

	public CSRFInterceptor(ObjectMapper objectMapper) throws JsonProcessingException {
		failMessage = objectMapper.writeValueAsString(Map.of("message", "CRSF校验失败"));
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod))
			return true;

		//在配置文件里可以关闭CSRF检验
		if (!csrfVerify) return true;

		//目前来说，所有需要防止伪造的请求只针对登录的用户
		if (SecurtyContext.getCurrentUser() == null) return true;

		Object csrf = request.getSession().getAttribute(ATTR_NAME);
		if (csrf != null && csrf.equals(request.getHeader("X-CSRF-Token"))) {
			return true;
		}
		response.setStatus(403);
		response.setHeader("Content-Type", "application/json;charset=UTF-8");
		response.getWriter().write(failMessage);
		return false;
	}

}
