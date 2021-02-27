package com.kaciras.blog.infra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.UncheckedIOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class RequestUtilsTest {

	@Test
	void addressFromRequest() {
		var request = new MockHttpServletRequest();

		var address = RequestUtils.addressFrom(request);
		assertThat(request.getRemoteAddr()).isNotNull();
		assertThat(address.isLoopbackAddress()).isTrue();

		request.setRemoteAddr("1234::5678");
		address = RequestUtils.addressFrom(request);
		assertThat(address).isInstanceOf(Inet6Address.class);

		request.setRemoteAddr(null);
		address = RequestUtils.addressFrom(request);
		assertThat(address.isLoopbackAddress()).isTrue();
	}

	@Test
	void addressFromRequestUnknownHost() {
		var request = new MockHttpServletRequest();
		request.setRemoteAddr("invalid");
		assertThatThrownBy(() -> RequestUtils.addressFrom(request)).isInstanceOf(UncheckedIOException.class);
	}

	private static Stream<Arguments> addressAndIsLocal() {
		return Stream.of(
				Arguments.of("127.0.0.1", true),
				Arguments.of("192.168.0.1", true),
				Arguments.of("localhost", true),
				Arguments.of("[::1]", true),
				Arguments.of("1.1.1.1", false),
				Arguments.of("[1234::1]", false)
		);
	}

	@MethodSource("addressAndIsLocal")
	@ParameterizedTest
	void isLocalNetwork(String host, boolean expect) throws Exception {
		var address = InetAddress.getByName(host);
		var actual = RequestUtils.isLocalNetwork(address);
		assertThat(actual).isEqualTo(expect);
	}
}
