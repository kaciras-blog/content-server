package net.kaciras.blog.domain.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConfigBinding {

	private final ConfigService configService;
	private final MessageClient messageClient;

	private final ConvertorRegistery registery;

	@SuppressWarnings("unchecked")
	public <T> ConfigItem<T> get(String key, Class<T> type) {
		return new ConfigItem<>(messageClient, configService, registery, type, key);
	}
}
