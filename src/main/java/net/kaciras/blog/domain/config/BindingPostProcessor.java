package net.kaciras.blog.domain.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.ConfigBind;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@RequiredArgsConstructor
@Component
public class BindingPostProcessor implements BeanPostProcessor {

	private final ConfigBinding configBinding;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Method[] methods = bean.getClass().getDeclaredMethods();
		for (Method method : methods) {
			method.setAccessible(true);
			ConfigBind bind = method.getDeclaredAnnotation(ConfigBind.class);
			if (bind == null) {
				continue;
			}
			Parameter[] parameters = method.getParameters();
			if (parameters.length != 1) {
				throw new BeanInitializationException("无法绑定配置项");
			}

			ConfigItem item = configBinding.get(bind.value(), parameters[0].getType());
			item.bind(value -> {
				try {
					method.invoke(bean, value);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		}
		return bean;
	}
}
