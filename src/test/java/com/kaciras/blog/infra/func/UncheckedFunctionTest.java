package com.kaciras.blog.infra.func;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UncheckedFunctionTest {

	private Object throwingFunction(Object ignore) throws Exception {
		throw new IOException();
	}

	@Test
	void noThrows() {
		Function<Integer, Integer> cube = (UncheckedFunction<Integer, Integer>) (t) -> t * t * t;
		assertThat(cube.apply(4)).isEqualTo(4 * 4 * 4);
	}

	@Test
	void doThrows() {
		Function<Object, Object> throwing = (UncheckedFunction<Object, Object>) this::throwingFunction;

		assertThatThrownBy(() -> throwing.apply(null))
				.isInstanceOf(UncheckedFunctionException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	void uncheckedHelpers() {
		Function<?, ?> function = FunctionUtils.uncheckedFn(this::throwingFunction);
		assertThat(function).isInstanceOf(UncheckedFunction.class);
	}
}
