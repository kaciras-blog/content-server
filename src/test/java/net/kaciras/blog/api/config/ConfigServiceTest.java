package net.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;

@SpringBootTest(classes = ConfigServiceTest.TestConfig.class)
public class ConfigServiceTest {

	@Import(JacksonAutoConfiguration.class)
	@ComponentScan
	@Configuration
	static class TestConfig {}

	@Autowired
	private ConfigService configService;

	@Autowired
	private TestBindingBean testBean;

	@MockBean
	private ConfigStore configStore;

	@Test
	void testFire() {
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(33);
		configService.set("test.int", "-123");
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(-123);
	}

	/** 测试能够将枚举名转换为枚举值 */
	@Test
	void testEnumDeserialize() {
		configService.set("test.enum", "\"PACKAGE\"");
		Assertions.assertThat(testBean.getEnumValue()).isEqualTo(ElementType.PACKAGE);
	}

	@Test
	void testInit() {
		Assertions.assertThat(testBean.getInitValue()).isEqualTo(1.5);
	}
}
