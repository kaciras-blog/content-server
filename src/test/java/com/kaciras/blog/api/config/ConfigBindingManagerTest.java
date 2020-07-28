package com.kaciras.blog.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ValidationException;
import javax.validation.Validator;

@Import({JacksonAutoConfiguration.class, ValidationAutoConfiguration.class})
@ActiveProfiles("test")
@SpringBootTest
class ConfigBindingManagerTest {

	@Autowired
	private Validator validator;

	@MockBean
	private ConfigRepository repository;

	// 每个测试都重新创建，避免bind方法污染
	private ConfigBindingManager configBindingManager;

	private TestBindingConfig config;

	@BeforeEach
	void setUp() {
		configBindingManager = new ConfigBindingManager(repository, validator);
		configBindingManager.bind("test", TestBindingConfig.class, v -> config = v);
	}

	@Test
	void testBindInit() {
		Assertions.assertThat(config.getIntValue()).isEqualTo(33);
	}

	@Test
	void bindUpdate() {
		var newConfig = new TestBindingConfig();
		newConfig.setIntValue(-123);
		configBindingManager.set("test", newConfig);

		Assertions.assertThat(config.getIntValue()).isEqualTo(-123);
	}

	@Test
	void bindInvalidType() {
		Assertions.assertThatThrownBy(() -> configBindingManager.set("test", null))
				.isInstanceOf(NullPointerException.class);

		Assertions.assertThatThrownBy(() -> configBindingManager.set("test", Boolean.FALSE))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getWithoutBind() {
		Assertions.assertThat(configBindingManager.<Object>get("not.bind")).isNull();
	}

	/** ConfigBindingManager.get 会创建一个新对象 */
	@Test
	void isolation() {
		Assertions.assertThat(configBindingManager.<TestBindingConfig>get("test")).isNotSameAs(config);
	}

	@Test
	void jsr303Validate() {
		var newConfig = new TestBindingConfig();
		newConfig.setSubConfig(null);

		Assertions.assertThatThrownBy(() -> configBindingManager.set("test", newConfig))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void customValidation() {
		var newConfig = new TestBindingConfig();
		newConfig.setSmaller(newConfig.getBigger() + 666);

		Assertions.assertThatThrownBy(() -> configBindingManager.set("test", newConfig))
				.isInstanceOf(ValidationException.class);
	}
}
