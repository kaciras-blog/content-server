package com.kaciras.blog.infra.func;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UncheckedBiFunctionTest {

	private Object throwingFunction(Object ignore0, Object ignore1) throws Exception {
		throw new IOException();
	}

	@Test
	void noThrows() {
		BiFunction<Integer, Integer, Integer> cube = (UncheckedBiFunction<Integer, Integer, Integer>) (t, u) -> t * u;
		assertThat(cube.apply(4, 8)).isEqualTo(32);
	}

	@Test
	void doThrows() {
		BiFunction<Object, Object, Object> throwing =
				(UncheckedBiFunction<Object, Object, Object>) this::throwingFunction;

		assertThatThrownBy(() -> throwing.apply(null, null))
				.isInstanceOf(UncheckedFunctionException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	void uncheckedHelpers() {
		BiFunction<?, ?, ?> BiFunction = FunctionUtils.uncheckedFn(this::throwingFunction);
		assertThat(BiFunction).isInstanceOf(UncheckedBiFunction.class);
	}
}
