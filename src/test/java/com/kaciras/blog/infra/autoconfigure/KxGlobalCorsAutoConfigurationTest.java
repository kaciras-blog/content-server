package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.FilterChainCapture;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.Filter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class KxGlobalCorsAutoConfigurationTest {

	private final CorsProperties config = new CorsProperties();

	private Filter createFilter() {
		return new KxGlobalCorsAutoConfiguration(config).corsFilter().getFilter();
	}

	@Test
	void defaults() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.Default);

		var request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.addHeader("Origin", "https://example.com");

		var result = FilterChainCapture.doFilter(createFilter(), request);

		assertThat(result.outRequest).isSameAs(request);
		assertThat(result.inResponse.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	void allowedMethods() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.Default);
		config.setAllowedMethods(List.of("POST"));

		var request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.addHeader("Origin", "https://example.com");

		var result = FilterChainCapture.doFilter(createFilter(), request);
		assertThat(result.outRequest).isNull();
		assertThat(result.inResponse.getStatus()).isEqualTo(403);
	}

	@Test
	void exposedHeaders() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.Default);
		config.setExposedHeaders(List.of("X-Custom"));

		var request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.addHeader("Origin", "https://example.com");

		var result = FilterChainCapture.doFilter(createFilter(), request);

		assertThat(result.outRequest).isSameAs(request);
		assertThat(result.inResponse.getHeader("Access-Control-Expose-Headers")).containsOnlyOnce("X-Custom");
	}

	// https://www.w3.org/TR/cors/#resource-preflight-requests
	@Test
	void preFlight() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.AllowAll);

		var request = new MockHttpServletRequest();
		request.setMethod("OPTIONS");
		request.addHeader("Origin", "https://example.com");
		request.addHeader("Access-Control-Request-Method", "POST");

		var result = FilterChainCapture.doFilter(createFilter(), request);
		assertThat(result.outRequest).isNull();

		var response = result.inResponse;
		assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://example.com");
		assertThat(response.getHeader("Access-Control-Allow-Methods")).isEqualTo("POST");
		assertThat(response.getHeader("Access-Control-Max-Age")).isEqualTo("86400");
	}

	@Test
	void preFlightWithInvalidOrigin() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.AllowAll);
		config.setAllowedOrigins(List.of("https://abc.com"));

		var request = new MockHttpServletRequest();
		request.setMethod("OPTIONS");
		request.addHeader("Origin", "https://example.com");
		request.addHeader("Access-Control-Request-Method", "POST");

		var result = FilterChainCapture.doFilter(createFilter(), request);
		assertThat(result.outRequest).isNull();
		assertThat(result.inResponse.getStatus()).isEqualTo(403);
	}

	@Test
	void preFlightWithInvalidHeader() throws Exception {
		config.setTemplate(CorsProperties.CorsTemplate.AllowAll);
		config.setAllowedHeaders(List.of("X-Custom"));

		var request = new MockHttpServletRequest();
		request.setMethod("OPTIONS");
		request.addHeader("Origin", "https://example.com");
		request.addHeader("Access-Control-Request-Method", "POST");
		request.addHeader("Access-Control-Request-Headers", "X-Disallow");

		var result = FilterChainCapture.doFilter(createFilter(), request);
		assertThat(result.outRequest).isNull();
		assertThat(result.inResponse.getStatus()).isEqualTo(403);
	}
}
