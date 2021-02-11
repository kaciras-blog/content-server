package com.kaciras.blog.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ValidationException;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class ConfigBindingManagerTest {

	@Autowired
	private Validator validator;

	@MockBean
	private ConfigRepository repository;

	private ConfigBindingManager manager;

	private TestBindingConfig config;

	@BeforeEach
	void setUp() {
		manager = new ConfigBindingManager(repository, validator);
		manager.bind("test", TestBindingConfig.class, v -> config = v);
	}

	@Test
	void testBindInit() {
		assertThat(config.getIntValue()).isEqualTo(33);
	}

	/**
	 * 测试无法创建的类型，非静态内部类需要在构造方法中传入外层实例。
	 */
	@SuppressWarnings("InnerClassMayBeStatic")
	final class ConstructorWithParam {}

	@Test
	void invalidConstructor() {
		assertThatThrownBy(() -> manager.bind("ctor", ConstructorWithParam.class, v -> {}));
	}

	@Test
	void bindUpdate() {
		var newConfig = new TestBindingConfig();
		newConfig.setIntValue(-123);
		manager.set("test", newConfig);

		assertThat(config.getIntValue()).isEqualTo(-123);
	}

	@Test
	void bindInvalidType() {
		assertThatThrownBy(() -> manager.set("test", null))
				.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> manager.set("test", Boolean.FALSE))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getWithoutBind() {
		assertThat(manager.<Object>get("not.bind")).isNull();
	}

	/** ConfigBindingManager.get 会创建一个新对象 */
	@Test
	void isolation() {
		assertThat(manager.<TestBindingConfig>get("test")).isNotSameAs(config);
	}

	@Test
	void jsr303Validate() {
		var newConfig = new TestBindingConfig();
		newConfig.setSubConfig(null);

		assertThatThrownBy(() -> manager.set("test", newConfig)).isInstanceOf(ValidationException.class);
	}

	@Test
	void noJsr303Validator() {
		var newConfig = new TestBindingConfig();
		newConfig.setSubConfig(null);

		var manager = new ConfigBindingManager(repository, null);
		manager.set("test", newConfig);
	}

	@Test
	void customValidation() {
		var newConfig = new TestBindingConfig();
		newConfig.setSmaller(newConfig.getBigger() + 666);

		assertThatThrownBy(() -> manager.set("test", newConfig)).isInstanceOf(ValidationException.class);
	}

	@Test
	void getConfigFromRepository() {
		var config = new TestBindingConfig();
		config.setIntValue(4396);
		when(repository.load(eq("test"), eq(TestBindingConfig.class))).thenReturn(config);

		var value = (TestBindingConfig) manager.get("test");

		assertThat(value.getIntValue()).isEqualTo(4396);
	}
}
