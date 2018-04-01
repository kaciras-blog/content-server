package net.kaciras.blog.domain.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ConfigBinding {

	private final ConfigService configService;
	private final MessageClient messageClient;

	private Collection<Converter> converters;

	@PostConstruct
	private void register() {
		converters = List.of(
				new EnumConvertor(),
				new FunctionConvertor<>(String.class, v -> v),
				new FunctionConvertor<>(Integer.TYPE, Integer::parseInt),
				new FunctionConvertor<>(Integer.class, Integer::parseInt),
				new FunctionConvertor<>(Boolean.TYPE, Boolean::parseBoolean),
				new FunctionConvertor<>(Boolean.class, Boolean::parseBoolean),
				new FunctionConvertor<>(Double.class, Double::parseDouble),
				new FunctionConvertor<>(Double.TYPE, Double::parseDouble),
				new FunctionConvertor<>(Long.TYPE, Long::parseLong),
				new FunctionConvertor<>(Long.class, Long::parseLong),
				new FunctionConvertor<>(Float.TYPE, Float::parseFloat),
				new FunctionConvertor<>(Float.class, Float::parseFloat)
		);
	}

	@SuppressWarnings("unchecked")
	public <T> ConfigItem<T> get(String key, Class<T> type) {
		Optional<Converter> converter = converters.stream().filter(conv -> conv.matchs(type)).findFirst();
		if (!converter.isPresent()) {
			throw new NoSuchElementException("没有符合该键和类型的配置项");
		}
		return new ConfigItem<>(messageClient, configService, type, key, converter.get());
	}
}
