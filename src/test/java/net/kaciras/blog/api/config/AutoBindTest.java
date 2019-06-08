package net.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AutoBindTest.TestConfig.class)
public class AutoBindTest {

	@Configuration
	static class TestConfig {

		@Bean
		public ConfigBindingPostProcessor processor(ConfigService configService) {
			return new ConfigBindingPostProcessor(configService);
		}

		@Bean
		public BindingBean bindingBean() {
			return new BindingBean();
		}
	}

	@SuppressWarnings("unused")
	private static final class BindingBean {

		@BindConfig("net.kaciras.config")
		private TestBindingConfig config;
	}

	@SuppressWarnings("unused")
	private static final class BindingBean2 {

		@BindConfig("net.kaciras.config")
		private final TestBindingConfig config = new TestBindingConfig();
	}

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private BindingBean bindingBean;

	@MockBean
	private ConfigService configService;

	@Test
	void bind() {
		Assertions.assertThat(bindingBean).isNotNull();
		Mockito.verify(configService).bind(eq("net.kaciras.config"), eq(TestBindingConfig.class), any());
	}

	@Test
	void failOnFinalField() {
		var definition = mock(BeanDefinition.class);
		when(definition.getScope()).thenReturn(BeanDefinition.SCOPE_SINGLETON);
		var context = mock(GenericApplicationContext.class);
		when(context.getBeanDefinition("beanName")).thenReturn(definition);

		var processor = new ConfigBindingPostProcessor(configService);
		processor.setApplicationContext(context);

		var bean = new BindingBean2();
		Assertions.assertThatThrownBy(() -> processor.postProcessBeforeInitialization(bean, "beanName"))
				.isInstanceOf(BeanInitializationException.class);
	}
}
