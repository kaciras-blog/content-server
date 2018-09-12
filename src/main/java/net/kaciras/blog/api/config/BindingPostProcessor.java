package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.ConfigBind;
import net.kaciras.blog.api.ConfigBinding;
import net.kaciras.blog.api.ConfigItem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@RequiredArgsConstructor
@Component
class BindingPostProcessor implements BeanPostProcessor {

	private final ConfigBinding configBinding;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Class<?> clazz = bean.getClass();

		//method binding
		Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
			ConfigBind bind = method.getDeclaredAnnotation(ConfigBind.class);
			if(bind == null) return;
			method.setAccessible(true);
			ConfigItem item = configBinding.get(bind.value(), method.getParameterTypes()[0]);
			item.bind(bean, method);
		});

		//field binding
		Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
			ConfigBind bind = field.getDeclaredAnnotation(ConfigBind.class);
			if(bind == null) return;
			field.setAccessible(true);
			configBinding.get(bind.value(), field.getType()).bind(bean, field);
		});

		return bean;
	}
}