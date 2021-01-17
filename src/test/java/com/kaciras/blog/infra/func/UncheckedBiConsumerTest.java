package com.kaciras.blog.infra.func;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UncheckedBiConsumerTest {

	private void throwingBiConsumer(Object ignore0, Object ignore1) throws Exception {
		throw new IOException();
	}

	@Test
	void noThrows() {
		var holder = new AtomicReference<Integer>();
		BiConsumer<Integer, Integer> setter = (UncheckedBiConsumer<Integer, Integer>) (t,u) -> holder.set(t*u);

		setter.accept(4, 8);
		assertThat(holder.get()).isEqualTo(32);
	}

	@Test
	void doThrows() {
		BiConsumer<Object, Object> throwing = (UncheckedBiConsumer<Object, Object>) this::throwingBiConsumer;

		assertThatThrownBy(() -> throwing.accept(null, null))
				.isInstanceOf(UncheckedFunctionException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	void uncheckedHelpers() {
		BiConsumer<?, ?> consumer = FunctionUtils.unchecked(this::throwingBiConsumer);
		assertThat(consumer).isInstanceOf(UncheckedBiConsumer.class);
	}
}
