package com.kaciras.blog.infra;

import com.kaciras.blog.infra.exception.WebBusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.kaciras.blog.infra.TestHelper.getSubClassesInPackage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ExceptionResolverTest {

	@SuppressWarnings("unchecked")
	private static final List<Class<? extends WebBusinessException>> EXCEPTIONS =
			getSubClassesInPackage(WebBusinessException.class, "com.kaciras.blog.infra.exception");

	@SuppressWarnings({"ConstantConditions", "rawtypes"})
	@Test
	void handleNotDebug() throws Exception {
		var resolver = new ExceptionResolver(false);

		for (var clazz : EXCEPTIONS) {
			var exception = clazz.getConstructor().newInstance();
			var response = resolver.handle(exception);
			var body = (Map) response.getBody();

			assertThat(response.getStatusCode().value()).isEqualTo(exception.statusCode());
			assertThat(body.get("message")).isEqualTo(exception.getMessage());
		}
	}

	@Test
	void throwUnhandlableException() {
		var resolver = new ExceptionResolver(false);
		var e = new IOException();
		assertThatThrownBy(() -> resolver.handle(e)).isEqualTo(e);
	}

	@Test
	void respondErrorMessage() throws Exception {
		var resolver = new ExceptionResolver(false);

		var e = new BindException(resolver, "debug");
		var response = (ResponseEntity) resolver.handle(e);
		var message = ((Map<String, String>) response.getBody()).get("message");

		assertThat(message).isEqualTo(ExceptionResolver.DEFAULT_MESSAGE);
	}
}
