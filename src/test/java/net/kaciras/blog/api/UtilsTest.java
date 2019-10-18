package net.kaciras.blog.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.Inet6Address;

final class UtilsTest {

	@Test
	void addressFromRequest() {
		var request = new MockHttpServletRequest();

		var address = Utils.addressFromRequest(request);
		Assertions.assertThat(request.getRemoteAddr()).isNotNull();
		Assertions.assertThat(address.isLoopbackAddress()).isTrue();

		request.setRemoteAddr("1234::5678");
		address = Utils.addressFromRequest(request);
		Assertions.assertThat(address).isInstanceOf(Inet6Address.class);

		request.setRemoteAddr(null);
		address = Utils.addressFromRequest(request);
		Assertions.assertThat(address.isLoopbackAddress()).isTrue();
	}
}
