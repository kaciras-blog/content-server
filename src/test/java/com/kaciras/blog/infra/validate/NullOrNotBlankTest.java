package com.kaciras.blog.infra.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Iterables.getLast;
import static org.assertj.core.api.Assertions.assertThat;

public class NullOrNotBlankTest {

	@AllArgsConstructor
	private static final class FieldHost {

		@SuppressWarnings("unused")
		@NullOrNotBlank
		private final String value;
	}

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@AfterEach
	void cleanUp() {
		factory.close();
	}

	@Test
	void allowNull() {
		assertThat(validator.validate(new FieldHost(null))).isEmpty();
	}

	@Test
	void allowBotBlank() {
		assertThat(validator.validate(new FieldHost("foobar"))).isEmpty();
	}

	@Test
	void rejectBlank() {
		var violations = validator.validate(new FieldHost(""));

		assertThat(violations).hasSize(1);
		assertThat(getLast(violations).getPropertyPath().toString()).isEqualTo("value");
	}
}
