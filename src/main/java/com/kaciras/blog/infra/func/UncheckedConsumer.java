package com.kaciras.blog.infra.func;

import java.util.function.Consumer;

/**
 * 此类解决了 java.util.function.Consumer 内部不能有 CheckedException 的问题。
 *
 * Java 的 CheckedException 机制使 Lambda 表达式的接口必须声明异常，而一些常用的接口
 * 如 Consumer、Function、Runnable 等都没有声明，这让它们在有异常时写的非常难受。
 *
 * 用法：
 * Consumer<Integer> c = (UncheckedConsumer<Integer>)(t) -> { throw new IOException(); };
 *
 * @param <T> 接收的参数类型
 */
@FunctionalInterface
public interface UncheckedConsumer<T> extends Consumer<T> {

	@Override
	default void accept(T argument) {
		try {
			acceptThrows(argument);
		} catch (Exception e) {
			throw new UncheckedFunctionException(e);
		}
	}

	void acceptThrows(T t) throws Exception;
}