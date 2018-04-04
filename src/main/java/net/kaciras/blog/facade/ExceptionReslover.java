package net.kaciras.blog.facade;

import net.kaciras.blog.infrastructure.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class ExceptionReslover implements HandlerExceptionResolver {

	private final Map<Class, Integer> errorCodeMap = Map.of(
			PermissionException.class, 403,
			ResourceDeletedException.class, 410,
			ResourceNotFoundException.class, 404,
			RequestArgumentException.class, 400,
			DataTooBigException.class, 413
	);

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
										 HttpServletResponse response,
										 Object handler, Exception ex) {
		Integer code = errorCodeMap.get(ex.getClass());
		if (code != null) {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setStatus(HttpStatus.valueOf(code));
			response.setStatus(code);
			return modelAndView;
		}
		return null;
	}

}
