package com.kaciras.blog.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
final class ChangeListener<T> {

	private final List<Consumer<T>> listeners = new ArrayList<>(1);

	@Getter
	private final Class<T> type;

	/**
	 * 把配置对象应用到每个绑定点上。
	 *
	 * <h2>没有做隔离</h2>
	 * 懒得为每个接受者复制一份了，所有地方都是同一个对象。
	 *
	 * @param value 配置对象
	 */
	public void fire(T value) {
		listeners.forEach(lis -> lis.accept(value));
	}

	public void add(Consumer<T> consumer) {
		listeners.add(consumer);
	}
}
