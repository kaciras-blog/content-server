package net.kaciras.blog.domain.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.ConfigChangedEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@RequiredArgsConstructor
public final class ConfigItem<T> {

	private final MessageClient messageClient;
	private final ConfigService configService;

	@Getter
	private final Class<T> type;

	private final String key;

	private final Converter<T> converter;

	public void bind(Consumer<T> consumer) {
		consumer.accept(converter.convert(configService.getProperty(key), type));
		messageClient.subscribe(ConfigChangedEvent.class, event -> {
			if (event.getKey().equals(key)) consumer.accept(converter.convert(event.getNewValue(), type));
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
		return converter.convert(configService.getProperty(key), type);
	}
}
