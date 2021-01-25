package com.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
final class AutoBindTest {

	@TestConfiguration(proxyBeanMethods = false)
	@RequiredArgsConstructor
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

	private final BeanDefinitionRegistry beanRegistry = mock(BeanDefinitionRegistry.class);
	private final ConfigBindingManager bindingManager = mock(ConfigBindingManager.class);

	private final ConfigBindingPostProcessor processor = new ConfigBindingPostProcessor(beanRegistry, bindingManager);

	@Test
	void postProcess() {
		var app = new AnnotationConfigApplicationContext(BindTestConfig.class);
		verify(app.getBean(ConfigBindingManager.class))
				.bind(eq("com.kaciras.config"), eq(TestBindingConfig.class), any());
	}

	@Test
	void prototypeBean() {
		var definition = mock(BeanDefinition.class);
		when(definition.getScope()).thenReturn(BeanDefinition.SCOPE_PROTOTYPE);
		when(beanRegistry.getBeanDefinition("beanName")).thenReturn(definition);

		var bean = new BindingBean();
		processor.postProcessBeforeInitialization(bean, "beanName");

		assertThat(bean.config).isNull();
	}

	@Test
	void failOnFinalField() {
		var definition = mock(BeanDefinition.class);
		when(definition.getScope()).thenReturn(BeanDefinition.SCOPE_SINGLETON);
		when(beanRegistry.getBeanDefinition("beanName")).thenReturn(definition);

		var bean = new FinalFieldBean();
		Assertions.assertThatThrownBy(() -> processor.postProcessBeforeInitialization(bean, "beanName"))
				.isInstanceOf(BeanInitializationException.class);
	}
}
