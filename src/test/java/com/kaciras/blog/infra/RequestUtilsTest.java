package com.kaciras.blog.infra;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.Inet6Address;

final class RequestUtilsTest {

	@Test
	void addressFromRequest() {
		var request = new MockHttpServletRequest();

		var address = RequestUtils.addressFromRequest(request);
		Assertions.assertThat(request.getRemoteAddr()).isNotNull();
		Assertions.assertThat(address.isLoopbackAddress()).isTrue();

		request.setRemoteAddr("1234::5678");
		address = RequestUtils.addressFromRequest(request);
		Assertions.assertThat(address).isInstanceOf(Inet6Address.class);

		request.setRemoteAddr(null);
		address = RequestUtils.addressFromRequest(request);
		Assertions.assertThat(address.isLoopbackAddress()).isTrue();
	}
}
