package net.kaciras.blog.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.config.ConfigService;
import net.kaciras.blog.infrastructure.event.ConfigChangedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@RequiredArgsConstructor
public final class ConfigItem<T> {

	private final MessageClient messageClient;
	private final ConfigService configService;
	private final ConvertorRegistery convertorRegistery;

	@Getter
	private final Class<T> type;

	private final String key;

	public void bind(Consumer<T> consumer) {
		consumer.accept(convertorRegistery.convert(type, configService.getProperty(key)));
		messageClient.subscribe(ConfigChangedEvent.class, event -> {
			if (event.getKey().equals(key)) consumer.accept(convertorRegistery.convert(type, event.getNewValue()));
		});
	}

	public void bind(Object instance, Method method) {
		bind(value -> invokeSetter(instance, method, value));
	}

	//TODO：wrap to unchecked exception
	private void invokeSetter(Object instance, Method method, T value) {
		try {
			method.invoke(instance, value);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("配置项绑定失败", e);
		}
	}

	public void bind(Object instance, Field field) {
		bind(value -> setField(instance, field, value));
	}

	private void setField(Object instance, Field field, T value) {
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("配置项绑定失败", e);
		}
	}

	public T getValue() {
		return convertorRegistery.convert(type, configService.getProperty(key));
	}
}
