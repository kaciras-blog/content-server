package com.kaciras.blog.infra.validate;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static com.google.common.collect.Iterables.getLast;
import static org.assertj.core.api.Assertions.assertThat;

public class NullOrNotBlankTest {

	@AllArgsConstructor
	private static final class FieldHost {

		@SuppressWarnings("unused")
		@NullOrNotBlank
		private final String value;
	}

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void allowNull() {
		assertThat(validator.validate(new FieldHost(null))).isEmpty();
	}

	@Test
	void allowBotBlank() {
		assertThat(validator.validate(new FieldHost("foobar"))).isEmpty();
	}

	@Test
	void rejectBlank(){
		var violations = validator.validate(new FieldHost(""));

		assertThat(violations).hasSize(1);
		assertThat(getLast(violations).getPropertyPath().toString()).isEqualTo("value");
	}
}
