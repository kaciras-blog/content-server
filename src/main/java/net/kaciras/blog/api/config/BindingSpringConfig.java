package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Modifier;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RequiredArgsConstructor
public class BindingSpringConfig implements BeanPostProcessor, ApplicationContextAware {

	private final ConfigService configService;
	private BeanDefinitionRegistry beanRegistry;

	@Override
	public void setApplicationContext(ApplicationContext context) {
		beanRegistry = (BeanDefinitionRegistry) context;
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
				scanForBinding(bean, clazz, beanName);
			}
		} catch (NoSuchBeanDefinitionException ignore) {
			// 一些 Bean 触发了这个方法，但是在 BeanRegistry 里却没有定义
		}

		return bean;
	}

	public void scanForBinding(Object object, Class<?> clazz, String beanName) {
		for (var field : clazz.getDeclaredFields()) {
			var bind = field.getDeclaredAnnotation(BoundConfig.class);
			if (bind == null) {
				continue;
			}
			if ((field.getModifiers() & Modifier.FINAL) != 0) {
				throw new BeanInitializationException(beanName + "绑定的字段不能为final：" + field.getName());
			}
			field.setAccessible(true);
			configService.bind(bind.value(), field.getType(), value -> field.set(object, value));
		}
	}

}
