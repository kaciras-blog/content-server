package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.func.ThrowingConsumer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ConfigService {

	private final Map<String, ChangeListener> bindings = new HashMap<>();
	private final ConfigRepository configRepository;

	public <T> void bind(String name, Class<T> type, ThrowingConsumer<T> setter) {
		try {
			var listener = bindings.computeIfAbsent(name, __ -> new ChangeListener(type));
			listener.add(setter);

			var config = configRepository.load(name, type);
			if (config == null) {
				config = type.getConstructor().newInstance();
			}
			setter.accept(config);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("无法实例化配置：" + type, e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		var binding = bindings.get(name);
		if (binding == null) {
			throw new RuntimeException("未注册（绑定）的配置，你必须先绑定，" + name);
		}
		return (T) configRepository.load(name, binding.getType());
	}

	public void set(String name, Object value) {
		var binding = bindings.get(name);
		if (binding != null) {
			if (binding.getType() != value.getClass()) {
				throw new RuntimeException("配置类型不符");
			}
			binding.fire(value);
		}
		configRepository.save(name, value);
	}
}
