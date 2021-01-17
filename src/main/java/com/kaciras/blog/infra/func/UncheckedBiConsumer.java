package com.kaciras.blog.infra.func;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface UncheckedBiConsumer<T, U> extends BiConsumer<T, U> {

	@Override
	default void accept(T t, U u) {
		try {
			acceptThrows(t, u);
		} catch (Exception e) {
			throw new UncheckedFunctionException(e);
		}
	}

	void acceptThrows(T t, U u) throws Exception;
}
