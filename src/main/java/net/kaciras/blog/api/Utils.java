package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.exception.RequestArgumentException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public final class Utils {

	public static final Random RANDOM = new Random();

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static void checkNotNull(Object obj, String argName) {
		if (obj == null) throw new RequestArgumentException("参数" + argName + "不能为null");
	}

	public static InetAddress getAddress(String name) {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			throw new AssertionError("this method only be used for vaild address.");
		}
	}

	private Utils() {}
}
