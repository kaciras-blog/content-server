package net.kaciras.blog.api.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ConfigService implements BeanPostProcessor, InitializingBean {

	private final BeanDefinitionRegistry beanRegistry;
	private final ConfigStore configStore;

	// 无需使用线程安全的Map，因为它仅在启动时修改
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();

	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		var clazz = bean.getClass();

		// 只绑定单例 bean，因为没法知道原型 bean 什么时候销毁从而解绑
		var scope = beanRegistry.getBeanDefinition(beanName).getScope();
		if (!ConfigurableBeanFactory.SCOPE_PROTOTYPE.equals(scope)) {
			return bean;
		}

		// method binding
		for (var method : clazz.getDeclaredMethods()) {
			var bind = method.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}
			method.setAccessible(true);

			var parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new BeanInitializationException("绑定的方法必须只有一个参数：" + beanName + "，方法：" + method);
			}

			var type = parameterTypes[0];
			var listener = listenerMap.computeIfAbsent(bind.value(), __ -> new ChangeListener(type));
			if (listener.getType() != type) {
				throw new BeanInitializationException("绑定的配置类型不一致：" + beanName + "，方法：" + method);
			}

			listener.add(value -> method.invoke(bean, value));
		}

		for (var field : clazz.getDeclaredFields()) {
			var bind = field.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}
			field.setAccessible(true);

			var type = field.getType();
			var listener = listenerMap.computeIfAbsent(bind.value(), __ -> new ChangeListener(type));
			if (listener.getType() != type) {
				throw new BeanInitializationException("绑定的配置类型不一致：" + beanName + "，字段：" + field);
			}

			listener.add(value -> field.set(bean, value));
		}

		return bean;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		for (var prop : configStore) {
			var listener = listenerMap.get(prop.key);
			if (listener != null) {
				listener.fire(prop.value);
			}
		}
	}

	public <T> T get(String name, Class<T> type, T defau1t) {
		return null;
	}

	public void set(String name, Object value) {
		configStore.save(name, value);
		var listener = listenerMap.get(name);
		if (listener != null) {
			listener.fire(value);
		}
	}

}
