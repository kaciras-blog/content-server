package com.kaciras.blog.infra;

import com.kaciras.blog.infra.exception.WebBusinessException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import java.util.stream.Stream;

import static com.kaciras.blog.infra.TestHelper.getSubClassesInPackage;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = ExceptionResolverTest.TestConfiguration.class)
@WebMvcTest
final class ExceptionResolverTest {

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		TestController controller(){
			return new TestController();
		}

		@Bean
		ExceptionResolver resolver(){
			return new ExceptionResolver();
		}
	}

	@RestController
	static final class TestController {

		private WebBusinessException webException;

		@GetMapping("/web")
		void throwWebException() {
			throw webException;
		}

		@GetMapping("/validate")
		void validateException(@Valid TestParams params) {}
	}

	@RequiredArgsConstructor
	private static class TestParams {

		@Max(1)
		public final int value;
	}

	@Autowired
	private TestController controller;

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings({"unchecked"})
	private static Stream<Arguments> webExceptions() {
		var pkg = "com.kaciras.blog.infra.exception";
		return getSubClassesInPackage(WebBusinessException.class, pkg).map(Arguments::of);
	}

	@MethodSource("webExceptions")
	@ParameterizedTest
	void handleNotDebug(Class<? extends WebBusinessException> clazz) throws Exception {
		var e = clazz.getConstructor().newInstance();
		controller.webException = e;

		mockMvc.perform(get("/web"))
				.andExpect(status().is(e.statusCode()))
				.andExpect(jsonPath("$.message").value(e.getMessage()));
	}

	@Test
	void jsr303Exception() throws Exception {
		mockMvc.perform(get("/validate").param("value", "2"))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.message").value(ExceptionResolver.DEFAULT_MESSAGE));
	}

	@Test
	void typeMismatch() throws Exception {
		mockMvc.perform(get("/validate").param("value", "str"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(ExceptionResolver.DEFAULT_MESSAGE));
	}
}
