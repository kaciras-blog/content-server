package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RequiredArgsConstructor
@Service
public class ConfigService implements BeanPostProcessor, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	/** 适配 listenerMap 里不存在的键 */
	private static final ChangeListener EMPTY = new ChangeListener(Object.class);

	// 无需使用线程安全的Map，因为它仅在启动时修改
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();

	private final ConfigStore configStore;
	private final ObjectMapper objectMapper;

	private BeanDefinitionRegistry beanRegistry;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.beanRegistry = (BeanDefinitionRegistry) context;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		var clazz = bean.getClass();

		if (!clazz.getName().startsWith("net.kaciras")) {
			return bean;
		}

		String scope;
		try {
			scope = beanRegistry.getBeanDefinition(beanName).getScope();
		} catch (NoSuchBeanDefinitionException ignore) {
			return bean;
		}

		/*
		 * 目前只绑定单例 bean，因为没法知道原型 bean 什么时候销毁从而解绑。
		 * 另外原型bean可能频繁创建，每次注入都从数据库读取性能差。当前也没有用原型bean
		 */
		if (SCOPE_SINGLETON.equals(scope) || "".equals(scope)) {

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
		}
		return bean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		configStore.loadAll().forEach(p -> listenerMap.getOrDefault(p.key, EMPTY).fire(p.value));
	}

	private <T> T deserialize(String string, Class<T> type) {
		try {
			return objectMapper.readValue(string, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T get(String name, Class<T> type, T defau1t) {
		var value = configStore.load(Collections.singletonList(name));
		if (value.isEmpty()) {
			return defau1t;
		}
		return deserialize(value.get(0), type);
	}

	public List<String> batchGet(List<String> keys) {
		return configStore.load(keys);
	}

	public void set(String name, String value) {
		batchSet(Map.of(name, value));
	}

	public void batchSet(Map<String, String> properties) {
		configStore.save(properties);
		for (var e : properties.entrySet()) {
			var listener = listenerMap.get(e.getKey());
			if (listener == null) {
				continue;
			}
			listener.fire(deserialize(e.getValue(), listener.getType()));
		}
	}
}
