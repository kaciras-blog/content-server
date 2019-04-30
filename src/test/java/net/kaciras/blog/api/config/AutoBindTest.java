package net.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private BindingBean bindingBean;

	@MockBean
	private ConfigService configService;

	@Test
	void test() {
		Assertions.assertThat(bindingBean).isNotNull();
		Mockito.verify(configService).bind(eq("net.kaciras.config"), eq(TestBindingConfig.class), any());
	}
}
