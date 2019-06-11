package net.kaciras.blog.api.config;

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

	@Configuration
	static class BindTestConfig {

		@Bean
		public ConfigService configService() {
			return mock(ConfigService.class);
		}

		@Bean
		public ConfigBindingPostProcessor processor(ConfigService configService) {
			return new ConfigBindingPostProcessor(configService);
		}

		@Bean
		public BindingBean bindingBean() {
			return new BindingBean();
		}
	}

	private static final class BindingBean {

		@BindConfig("net.kaciras.config")
		private TestBindingConfig config;
	}

	private static final class FinalFieldBean {

		@BindConfig("final.field")
		private final TestBindingConfig config = new TestBindingConfig();
	}

	@Test
	void bind() {
		var app = new AnnotationConfigApplicationContext(BindTestConfig.class);
		Mockito.verify(app.getBean(ConfigService.class))
				.bind(eq("net.kaciras.config"), eq(TestBindingConfig.class), any());
	}

	@Test
	void failOnFinalField() {
		var service = mock(ConfigService.class);

		var definition = mock(BeanDefinition.class);
		when(definition.getScope()).thenReturn(BeanDefinition.SCOPE_SINGLETON);
		var context = mock(GenericApplicationContext.class);
		when(context.getBeanDefinition("beanName")).thenReturn(definition);

		var processor = new ConfigBindingPostProcessor(service);
		processor.setApplicationContext(context);

		var bean = new FinalFieldBean();
		Assertions.assertThatThrownBy(() -> processor.postProcessBeforeInitialization(bean, "beanName"))
				.isInstanceOf(BeanInitializationException.class);
	}
}
