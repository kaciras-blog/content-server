package net.kaciras.blog.domain.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.ConfigChangedEvent;

import java.util.function.Consumer;

@RequiredArgsConstructor
public final class ConfigItem<T> {

	private final MessageClient messageClient;
	private final ConfigService configService;

	private final String key;
	private final Class<T> type;
	private final Converter<T> converter;

	public void bind(Consumer<T> consumer) {
		consumer.accept(converter.convert(configService.getProperty(key), type));
		messageClient.subscribe(ConfigChangedEvent.class, event -> {
			if (event.getKey().equals(key)) consumer.accept(converter.convert(event.getNewValue(), type));
		});
	}

	public T getValue() {
		return converter.convert(configService.getProperty(key), type);
	}
}
