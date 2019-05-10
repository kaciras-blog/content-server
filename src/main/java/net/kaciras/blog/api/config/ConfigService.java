package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.func.UncheckedConsumer;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 目前还没彻底跟Spring分离
 * 【注意】懒得再把参数复制一份了，所以该方法不具有隔离性。
 */
@RequiredArgsConstructor
@Service
public class ConfigService {

	// 目前仅在启动时修改，不存在线程安全问题
	private final Map<String, ChangeListener> bindings = new HashMap<>();

	private final Validator validator;
	private final ConfigRepository configRepository;

	public <T> void bind(String name, Class<T> type, UncheckedConsumer<T> setter) {
		setter.accept(getConfigObject(name, type));
		var listener = bindings.computeIfAbsent(name, (__) -> new ChangeListener(type));
		listener.add(setter);
	}

	/**
	 * 根据配置名获取配置对象，该配置对象是新建的，对其所做的修改不会直接影响配置的使用者。
	 * 若要应用修改，请使用 set 方法显示应用某个配置对象。
	 * 返回的配置对象始终从存储中加载，而不是某个正在使用的对象，使用 set 以外的方法修改配置将不影响此犯法的返回值。
	 *
	 * @param name 配置名
	 * @param <T>  配置对象的类型
	 * @return 配置对象，如果不存在绑定记录则返回null
	 */
	@SuppressWarnings("unchecked")
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
	 * @param value 新的配置，不能为null
	 */
	public void set(String name, Object value) {
		validate(name, Objects.requireNonNull(value));

		var binding = bindings.get(name);
		if (binding != null) {
			if (binding.getType() != value.getClass()) {
				throw new IllegalArgumentException("配置类型不符");
			}
			binding.fire(value);
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
		var errors = validator.validate(value);
		if (!errors.isEmpty()) {
			throw new ConstraintViolationException(errors);
		}
	}

	// 从存储中加载，如果存储中没有则使用默认构造方法创建
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
