package net.kaciras.blog.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
final class ChangeListener {

	private final List<Consumer> listeners = new ArrayList<>(1);

	@Getter
	private final Class<?> type;

	/**
	 * 把配置对象应用到每个绑定点上。
	 *【注意】懒得为每个接受者复制一份了，所以该方法不具有隔离性。
	 *
	 * @param value 配置对象
	 */
	public void fire(Object value) {
		listeners.forEach(lis -> lis.accept(value));
	}

	public void add(Consumer consumer) {
		listeners.add(consumer);
	}
}
