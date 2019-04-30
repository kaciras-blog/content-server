package net.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.validation.ValidationException;
import javax.validation.Validator;

@Import({JacksonAutoConfiguration.class, ValidationAutoConfiguration.class})
@SpringBootTest
@SpringBootConfiguration
public class ConfigServiceTest {

	@Autowired
	private Validator validator;

	@MockBean
	private ConfigRepository repository;

	// 每个测试都重新创建，避免bind方法污染
	private ConfigService configService;

	private TestBindingConfig config;

	@BeforeEach
	void setUp() {
		configService = new ConfigService(validator, repository);
		configService.bind("test", TestBindingConfig.class, v -> config = v);
	}

	@Test
	void testBindInit() {
		Assertions.assertThat(config.getIntValue()).isEqualTo(33);
	}

	@Test
	void testBindUpdate() {
		var newConfig = new TestBindingConfig();
		newConfig.setIntValue(-123);
		configService.set("test", newConfig);

		Assertions.assertThat(config.getIntValue()).isEqualTo(-123);
	}

	@Test
	void testBindInvalidType() {
		Assertions.assertThatThrownBy(() -> configService.set("test", null))
				.isInstanceOf(NullPointerException.class);

		Assertions.assertThatThrownBy(() -> configService.set("test", Boolean.FALSE))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testGetWithoutBind() {
		Assertions.assertThat(configService.<Object>get("not.bind")).isNull();
	}

	/** ConfigService.get 会创建一个新对象 */
	@Test
	void testIsolation() {
		Assertions.assertThat(configService.<TestBindingConfig>get("test")).isNotSameAs(config);
	}

	@Test
	void testJsr303Validate() {
		var newConfig = new TestBindingConfig();
		newConfig.setSubConfig(null);

		Assertions.assertThatThrownBy(() -> configService.set("test", newConfig))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void testCustomValidate() {
		var newConfig = new TestBindingConfig();
		newConfig.setSmaller(newConfig.getBigger() + 666);

		Assertions.assertThatThrownBy(() -> configService.set("test", newConfig))
				.isInstanceOf(ValidationException.class);
	}
}
