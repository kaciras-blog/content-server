package net.kaciras.blog.api.config;

import org.springframework.beans.factory.BeanInitializationException;

import java.util.HashMap;
import java.util.Map;

public final class BindingRegistry {

	// 无需使用线程安全的Map，因为它仅在启动时修改
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();

	public void scanForBinding(Object bean) {
		var clazz = bean.getClass();

		for (var method : clazz.getDeclaredMethods()) {
			var bind = method.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}

			var parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new BeanInitializationException("绑定的方法(" + method + ")必须只有一个参数");
			}

			var type = parameterTypes[0];
			var listener = listenerMap.computeIfAbsent(bind.value(), __ -> new ChangeListener(type));
			if (listener.getType() != type) {
				throw new BeanInitializationException("绑定的方法(" + method + ")与已存在的类型不一致");
			}

			method.setAccessible(true);
			listener.add(value -> method.invoke(bean, value));
		}

		for (var field : clazz.getDeclaredFields()) {
			var bind = field.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}

			var type = field.getType();
			var listener = listenerMap.computeIfAbsent(bind.value(), __ -> new ChangeListener(type));
			if (listener.getType() != type) {
				throw new BeanInitializationException("绑定的字段(" + field + ")与已存在的类型不一致");
			}

			field.setAccessible(true);
			listener.add(value -> field.set(bean, value));
		}
	}

	public void update(String key, Object value) {
		var listener = listenerMap.get(key);
		if (listener != null) {
			listener.fire(value);
		}
	}
}
