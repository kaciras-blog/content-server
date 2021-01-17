package com.kaciras.blog.infra.validate;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getLast;
import static org.assertj.core.api.Assertions.assertThat;

final class HttpURIValidatorTest {

	@AllArgsConstructor
	private static final class FieldHost {

		@SuppressWarnings("unused")
		@HttpURI
		private final URI uri;
	}

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void allowNull() {
		assertThat(validator.validate(new FieldHost(null))).isEmpty();
	}

	private static Stream<Arguments> invalidValues() {
		return Stream.of(
				"/only/path?key=value#fragment",
				"",
				"https:/",
				"example.com",
				"ftp://example.com"
		).map(URI::create).map(Arguments::of);
	}

	@MethodSource("invalidValues")
	@ParameterizedTest
	void invalid(URI value) {
		var violations = validator.validate(new FieldHost(value));

		assertThat(violations).hasSize(1);
		assertThat(getLast(violations).getPropertyPath().toString()).isEqualTo("uri");
	}

	private static Stream<Arguments> validValues() {
		return Stream.of(
				"http://foobar/p/a/t/h?key=value&a=b#fragment",
				"http://foobar",
				"https://foobar"
		).map(URI::create).map(Arguments::of);
	}

	@MethodSource("validValues")
	@ParameterizedTest
	void validValues(URI value) {
		assertThat(validator.validate(new FieldHost(value))).isEmpty();
	}
}
