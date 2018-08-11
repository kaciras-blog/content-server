package net.kaciras.blog.facade;

import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.infrastructure.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionReslover {

	/**
	 * 自己定义的异常
	 */
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

		/* 控制器方法的参数@Valid失败 */
		if (ex instanceof MethodArgumentNotValidException) {
//			List<ObjectError> errors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
			return ResponseEntity.status(400).body(Map.of("message", "请求参数或内容不合法"));
		}

		/* 控制器参数中对象的字段@Valid失败 */
		if (ex instanceof BindException) {
			return ResponseEntity.status(400).body(Map.of("message", "请求参数或内容不合法"));
		}
		throw ex;
	}

}
