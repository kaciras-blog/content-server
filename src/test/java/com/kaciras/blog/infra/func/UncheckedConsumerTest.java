package com.kaciras.blog.infra.func;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UncheckedConsumerTest {

	private void throwingConsumer(Object ignore) throws Exception {
		throw new IOException();
	}

	@Test
	void noThrows() {
		var holder = new AtomicReference<>();
		Consumer<Object> setter = (UncheckedConsumer<Object>) holder::set;

		setter.accept(UncheckedConsumerTest.class);
		assertThat(holder.get()).isEqualTo(UncheckedConsumerTest.class);
	}

	@Test
	void doThrows() {
		Consumer<Object> throwing = (UncheckedConsumer<Object>) this::throwingConsumer;

		assertThatThrownBy(() -> throwing.accept(null))
				.isInstanceOf(UncheckedFunctionException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	void uncheckedHelpers() {
		Consumer<?> consumer = FunctionUtils.unchecked(this::throwingConsumer);
		assertThat(consumer).isInstanceOf(UncheckedConsumer.class);
	}
}
