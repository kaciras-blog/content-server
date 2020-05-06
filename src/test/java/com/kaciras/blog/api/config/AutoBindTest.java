package com.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
class AutoBindTest {

	@RequiredArgsConstructor
	@Configuration
	static class BindTestConfig {

		private final GenericApplicationContext context;

		@Bean
		public ConfigBindingManager configService() {
			return mock(ConfigBindingManager.class);
		}

		@Bean
		public ConfigBindingPostProcessor processor(ConfigBindingManager bindingManager) {
			return new ConfigBindingPostProcessor(context, bindingManager);
		}

		@Bean
		public BindingBean bindingBean() {
			return new BindingBean();
		}
	}

	private static final class BindingBean {

		@BindConfig("com.kaciras.config")
		private TestBindingConfig config;
	}

	private static final class FinalFieldBean {

		@BindConfig("final.field")
		private final TestBindingConfig config = new TestBindingConfig();
	}

	@Test
	void bind() {
		var app = new AnnotationConfigApplicationContext(BindTestConfig.class);
		Mockito.verify(app.getBean(ConfigBindingManager.class))
				.bind(eq("com.kaciras.config"), eq(TestBindingConfig.class), any());
	}

	@Test
	void failOnFinalField() {
		var service = mock(ConfigBindingManager.class);

		var definition = mock(BeanDefinition.class);
		when(definition.getScope()).thenReturn(BeanDefinition.SCOPE_SINGLETON);
		var context = mock(GenericApplicationContext.class);
		when(context.getBeanDefinition("beanName")).thenReturn(definition);

		var processor = new ConfigBindingPostProcessor(context, service);

		var bean = new FinalFieldBean();
		Assertions.assertThatThrownBy(() -> processor.postProcessBeforeInitialization(bean, "beanName"))
				.isInstanceOf(BeanInitializationException.class);
	}
}
