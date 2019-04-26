package net.kaciras.blog.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.func.ThrowingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
final class ChangeListener {

	private final List<Consumer<Object>> listeners = new ArrayList<>(1);

	@Getter
	private final Class<?> type;

	public void fire(Object value) {
		listeners.forEach(lis -> lis.accept(value));
	}

	public void add(ThrowingConsumer<Object> consumer) {
		listeners.add(consumer);
	}
}
