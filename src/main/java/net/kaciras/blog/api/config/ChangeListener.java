package net.kaciras.blog.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.func.ThrowingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
final class ChangeListener<T> {

	private final List<Consumer<T>> listeners = new ArrayList<>(1);

	@Getter
	private final Class<T> type;

	public void fire(T value) {
		listeners.forEach(lis -> lis.accept(value));
	}

	public void add(ThrowingConsumer<T> consumer) {
		listeners.add(consumer);
	}
}
