package com.kaciras.blog.api.config;

import com.kaciras.blog.infra.func.UncheckedConsumer;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Service
public class ConfigBindingManager {

	// 目前仅在启动时添加，不存在线程安全问题
	private final Map<String, ChangeListener<?>> bindings = new HashMap<>();

	private final ConfigRepository configRepository;

	@Nullable
	private final Validator validator;

	public <T> void bind(String name, Class<T> type, UncheckedConsumer<T> setter) {
		setter.accept(getConfigObject(name, type));
		var listener = bindings.computeIfAbsent(name, (__) -> new ChangeListener<>(type));
		((ChangeListener<T>) listener).add(setter);
	}

	/**
	 * 根据配置名获取配置对象，返回值是新建的，对其所做的修改不会直接影响现有的配置。
	 * 若要修改现有的配置，请使用 set 方法。
	 *
	 * @param name 配置名
	 * @param <T>  配置对象的类型
	 * @return 配置对象，如果不存在绑定记录则返回 null
	 */
	public <T> T get(String name) {
		var binding = bindings.get(name);
		if (binding == null) {
			return null;
		}
		return (T) getConfigObject(name, binding.getType());
	}

	/**
	 * 设置指定的配置，新的配置将应用到所有绑定了的地方，并保存在存储中以便下次使用。
	 *
	 * @param name  配置名
	 * @param value 新的配置，不能为 null
	 */
	public <T> void set(String name, T value) {
		validate(name, Objects.requireNonNull(value));

		var binding = bindings.get(name);
		if (binding != null) {
			if (binding.getType() != value.getClass()) {
				throw new IllegalArgumentException("配置类型不符");
			}
			((ChangeListener<T>) binding).fire(value);
		}

		configRepository.save(name, value);
	}

	// 两种校验方式，以后可能会扩展？
	private void validate(String name, Object value) {
		if (value instanceof ValidatedConfig) {
			var success = ((ValidatedConfig) value).validate();
			if (!success) {
				throw new ValidationException("配置：" + name + "验证失败");
			}
		}
		if (validator != null) {
			var errors = validator.validate(value);
			if (!errors.isEmpty()) {
				throw new ConstraintViolationException(errors);
			}
		}
	}

	/**
	 * 尝试从存储中加载指定的没做，如果存储中没有则使用默认构造方法创建。
	 *
	 * @param name 配置名
	 * @param type 配置对象的类型
	 * @param <T>  垃圾 JAVA 不能直接从泛型读取 class
	 * @return 配置对象，不会为 null
	 */
	@NonNull
	private <T> T getConfigObject(String name, Class<T> type) {
		try {
			var config = configRepository.load(name, type);
			if (config == null) {
				var ctor = type.getDeclaredConstructor();
				ctor.setAccessible(true);
				config = ctor.newInstance();
			}
			validate(name, config);
			return config;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("无法实例化配置：" + type, e);
		}
	}
}
