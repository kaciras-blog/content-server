package com.kaciras.blog.infra;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class TestHelper {

	/**
	 * 获取一个包内所有指定类的子类，不包括指定的类本身。
	 *
	 * @param clazz 指定的类
	 * @param pkg   包名
	 * @return 类列表，没有泛型因为调用方可能要转换
	 */
	@SuppressWarnings({"UnstableApiUsage", "rawtypes"})
	public static <T> List getSubClassesInPackage(Class<T> clazz, String pkg) {
		try {
			return ClassPath
					.from(TestHelper.class.getClassLoader())
					.getTopLevelClasses(pkg)
					.stream()
					.map(ClassPath.ClassInfo::load)
					.filter(clazz::isAssignableFrom)
					.filter(c -> !c.equals(clazz))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new Error("getSubClassesInPackage方法有BUG", e);
		}
	}
}
