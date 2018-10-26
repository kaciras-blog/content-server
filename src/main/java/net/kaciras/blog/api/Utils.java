package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.exception.RequestArgumentException;

public final class Utils {

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static void checkNotNull(Object obj, String argName) {
		if (obj == null) throw new RequestArgumentException("参数" + argName + "不能为null");
	}

	private Utils() {}
}
