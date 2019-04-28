package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RequiredArgsConstructor
@Service
public class ConfigService implements BeanPostProcessor, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	private final ConfigRepository configRepository;
	private final ObjectMapper objectMapper;

	private final BindingRegistry bindingRegistry = new BindingRegistry();

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

		try {
			var scope = beanRegistry.getBeanDefinition(beanName).getScope();

			/*
			 * 目前只绑定单例 bean，因为没法知道原型 bean 什么时候销毁从而解绑。
			 * 另外原型bean可能频繁创建，每次注入都从数据库读取性能差。当前也没有用原型bean
			 */
			if (StringUtils.isEmpty(scope) || SCOPE_SINGLETON.equals(scope)) {
				bindingRegistry.scanForBinding(bean);
			}
		} catch (NoSuchBeanDefinitionException ignore) {
			// 一些 Bean 触发了这个方法，但是在 BeanRegistry 里却没有定义
		}

		return bean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		configRepository.loadAll()
				.forEach(p -> bindingRegistry.update(p.key, type -> deserialize(p.value, type)));
	}

	private <T> T deserialize(String string, Class<T> type) {
		try {
			return objectMapper.readValue(string, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T get(String name, Class<T> type, T defau1t) {
		var value = configRepository.load(Collections.singletonList(name));
		if (value.isEmpty()) {
			return defau1t;
		}
		return deserialize(value.get(0), type);
	}

	public List<String> batchGet(List<String> keys) {
		return configRepository.load(keys);
	}

	public void set(String name, String value) {
		batchSet(Map.of(name, value));
	}

	public void batchSet(Map<String, String> properties) {
		configRepository.save(properties);
		properties.forEach((k, v) -> bindingRegistry.update(k, type -> deserialize(v, type)));
	}
}
