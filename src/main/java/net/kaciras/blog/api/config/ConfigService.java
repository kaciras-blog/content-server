package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.func.ThrowingConsumer;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service // 目前还没彻底跟Spring分离
public class ConfigService {

	private final Map<String, ChangeListener> bindings = new HashMap<>();

	private final Validator validator;
	private final ConfigRepository configRepository;

	public <T> void bind(String name, Class<T> type, ThrowingConsumer<T> setter) {
		try {
			var config = configRepository.load(name, type);
			if (config == null) {
				config = type.getConstructor().newInstance();
			}
			validate(name, config);

			var listener = bindings.computeIfAbsent(name, __ -> new ChangeListener(type));
			listener.add(setter);
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
		validate(name, value);

		var binding = bindings.get(name);
		if (binding != null) {
			if (binding.getType() != value.getClass()) {
				throw new RuntimeException("配置类型不符");
			}
			binding.fire(value);
		}

		configRepository.save(name, value);
	}

	private void validate(String name, Object value) {
		if (value instanceof ValidatedConfig) {
			var success = ((ValidatedConfig) value).validate();
			if (!success) {
				throw new ValidationException("配置：" + name + "验证失败");
			}
		}
		var errors = validator.validate(value);
		if (!errors.isEmpty()) {
			throw new ConstraintViolationException(errors);
		}
	}
}
