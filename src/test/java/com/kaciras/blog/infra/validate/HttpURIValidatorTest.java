package com.kaciras.blog.infra.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static com.google.common.collect.Iterables.getLast;
import static org.assertj.core.api.Assertions.assertThat;

final class HttpURIValidatorTest {

	@AllArgsConstructor
	private static final class FieldHost {

		@SuppressWarnings("unused")
		@HttpURI
		private final URI uri;
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

	@ValueSource(strings = {
			"/only/path?key=value#fragment",
			"",
			"https:/",
			"example.com",
			"ftp://example.com"
	})
	@ParameterizedTest
	void invalid(String value) {
		var violations = validator.validate(new FieldHost(URI.create(value)));

		assertThat(violations).hasSize(1);
		assertThat(getLast(violations).getPropertyPath().toString()).isEqualTo("uri");
	}

	@ValueSource(strings = {
			"http://foobar/p/a/t/h?key=value&a=b#fragment",
			"http://foobar",
			"https://foobar"
	})
	@ParameterizedTest
	void validValues(String value) {
		assertThat(validator.validate(new FieldHost(URI.create(value)))).isEmpty();
	}
}
