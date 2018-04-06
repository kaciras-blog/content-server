package net.kaciras.blog.domain;

import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;

public final class Utils {

	public static final Random RANDOM = new Random();

	public static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * 用于检查Update，Delete等SQL语句是否产生了影响，没产生影响时将抛出异常
	 *
	 * @param rows 影响行数
	 * @throws ResourceNotFoundException 如果没有影响任何行
	 */
	public static void checkEffective(int rows) {
		if (rows <= 0) throw new ResourceNotFoundException();
	}

	public static <T> T checkNotNullResource(T obj) {
		if (obj == null)
			throw new ResourceNotFoundException();
		return obj;
	}

	public static <T> T checkNotNullResource(T obj, String message) {
		if (obj == null)
			throw new ResourceNotFoundException(message);
		return obj;
	}

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new IllegalArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new IllegalArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static void checkNotNull(Object obj, String argName) {
		if (obj == null) throw new IllegalArgumentException("参数" + argName + "不能为null");
	}

	public static <T> void validateArg(T object) {
		Set<ConstraintViolation<T>> violations = validator.validate(object);
		if (!violations.isEmpty()) {
			ConstraintViolation<T> first = violations.iterator().next();
			throw new IllegalArgumentException(first.getMessage());
		}
	}

	private Utils() {}
}
