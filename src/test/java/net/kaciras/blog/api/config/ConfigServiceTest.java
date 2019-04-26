package net.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.util.stream.Stream;

@ActiveProfiles("test")
@SpringBootTest(classes = ConfigServiceTest.TestConfig.class)
public class ConfigServiceTest {

	@Import(JacksonAutoConfiguration.class)
	@ComponentScan
	@Configuration
	static class TestConfig {

		@Bean
		public ConfigStore configStore() {
			var configStore = Mockito.mock(ConfigStore.class);
			Mockito.when(configStore.loadAll())
					.thenReturn(Stream.of(new ConfigStore.Property("test.init", "1.5")));
			return configStore;
		}
	}

	@Autowired
	private ConfigService configService;

	@Autowired
	private TestBindingBean testBean;

	@Test
	void testFire() {
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(33);
		configService.set("test.int", "-123");
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(-123);
	}

	/** 测试能够将枚举名转换为枚举值 */
	@Test
	void testEnumDeserialize() {
		Assertions.assertThat(testBean.getEnumValue()).isEqualTo(ElementType.FIELD);
		configService.set("test.enum", "\"PACKAGE\"");
		Assertions.assertThat(testBean.getEnumValue()).isEqualTo(ElementType.PACKAGE);
	}

	@Test
	void testInit() {
		Assertions.assertThat(testBean.getInitValue()).isEqualTo(1.5);
	}
}
