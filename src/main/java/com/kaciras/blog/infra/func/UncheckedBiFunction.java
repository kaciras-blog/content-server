package com.kaciras.blog.infra.func;

import java.util.function.BiFunction;

@FunctionalInterface
public interface UncheckedBiFunction<T, U, R> extends BiFunction<T, U, R> {

	@Override
	default R apply(T t, U u) {
		try {
			return applyThrows(t, u);
		} catch (Exception e) {
			throw new UncheckedFunctionException(e);
		}
	}

	R applyThrows(T t, U u) throws Exception;
}
