package net.kaciras.blog.facade;

import net.kaciras.blog.infrastructure.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@ControllerAdvice
@ResponseBody
public class ExceptionReslover {

	/** 自己定义的异常 */
	private final Map<Class, Integer> errorCodeMap = Map.of(
			RequestArgumentException.class, 400,
			PermissionException.class, 403,
			ResourceDeletedException.class, 410,
			ResourceNotFoundException.class, 404,
			DataTooBigException.class, 413,
			ResourceStateException.class, 412
	);

	@ExceptionHandler
	public ResponseEntity handle(Exception ex) throws Exception {
		Integer code = errorCodeMap.get(ex.getClass());
		if (code != null) {
			return ResponseEntity.status(code).body(Map.of("message", ex.getMessage()));
		}
		if(ex instanceof MethodArgumentNotValidException) {
//			List<ObjectError> errors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
			return ResponseEntity.status(400).body(Map.of("message", "请求中存在不合法的参数"));
		}
		throw ex;
	}

}
