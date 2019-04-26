package net.kaciras.blog.api.config;

import net.kaciras.blog.infrastructure.func.ThrowingConsumer;
import net.kaciras.blog.infrastructure.func.ThrowingFunction;
import org.springframework.beans.factory.BeanInitializationException;

import java.util.HashMap;
import java.util.Map;

public final class BindingRegistry {

	// 无需使用线程安全的Map，因为它仅在启动时修改
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();

	public void scanForBinding(Object object) {
		var clazz = object.getClass();

		for (var method : clazz.getDeclaredMethods()) {
			var bind = method.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}
			var parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new BeanInitializationException("绑定的方法(" + method + ")必须只有一个参数");
			}

			method.setAccessible(true);
			addToMap(bind.value(), value -> method.invoke(object, value), parameterTypes[0]);
		}

		for (var field : clazz.getDeclaredFields()) {
			var bind = field.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}
			field.setAccessible(true);
			addToMap(bind.value(), value -> field.set(object, value), field.getType());
		}
	}

	private void addToMap(String key, ThrowingConsumer<Object> setter, Class<?> type) {
		var listener = listenerMap.computeIfAbsent(key, __ -> new ChangeListener(type));
		if (listener.getType() != type) {
			throw new BeanInitializationException("绑定的属性(" + key + ")与已存在的类型不一致");
		}
		listener.add(setter);
	}

	public void update(String key, ThrowingFunction<Class<?>, Object> converter) {
		var lis = listenerMap.get(key);
		if (lis != null) {
			lis.fire(converter.apply(lis.getType()));
		}
	}
}
