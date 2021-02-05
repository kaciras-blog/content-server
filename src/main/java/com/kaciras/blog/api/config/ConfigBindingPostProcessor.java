package com.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.NonNull;

import java.lang.reflect.Modifier;

/**
 * 扫描所有 bean，将标记了{@link BindConfig}的字段绑定到{@link ConfigBindingManager}。
 *
 * @see BindConfig
 * @see ConfigBindingManager
 */
@RequiredArgsConstructor
final class ConfigBindingPostProcessor implements BeanPostProcessor {

	private final BeanDefinitionRegistry beanRegistry;
	private final ConfigBindingManager configBindingManager;

	@Override
	public Object postProcessBeforeInitialization(Object bean, @NonNull String beanName) {
		var clazz = bean.getClass();
		if (!clazz.getName().startsWith("com.kaciras")) {
			return bean;
		}

		/*
		 * 目前只绑定单例 bean，因为没法知道原型 bean 什么时候销毁从而解绑。
		 * 另外原型bean可能频繁创建，每次注入都从数据库读取性能差。当前也没有用原型bean
		 */
		try {
			var definition = beanRegistry.getBeanDefinition(beanName);
			if (BeanDefinition.SCOPE_PROTOTYPE.equals(definition.getScope())) {
				return bean;
			}
		} catch (NoSuchBeanDefinitionException ignore) {
			// 一些 Bean 触发了这个方法，但是在 BeanRegistry 里却没有定义
		}

		for (var field : clazz.getDeclaredFields()) {
			var bind = field.getDeclaredAnnotation(BindConfig.class);
			if (bind == null) {
				continue;
			}
			if ((field.getModifiers() & Modifier.FINAL) != 0) {
				throw new BeanInitializationException(beanName + " - 绑定的字段不能为final：" + field.getName());
			}
			field.setAccessible(true);
			configBindingManager.bind(bind.value(), field.getType(), value -> field.set(bean, value));
		}

		return bean;
	}
}
