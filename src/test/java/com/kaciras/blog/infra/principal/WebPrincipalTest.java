package com.kaciras.blog.infra.principal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class WebPrincipalTest {

	private static Stream<Arguments> getNameParams() {
		return Stream.of(
				Arguments.of(WebPrincipal.SYSTEM_ID, "System"),
				Arguments.of(WebPrincipal.ADMIN_ID, "Admin"),
				Arguments.of(5, "StandardUser:5"),
				Arguments.of(WebPrincipal.ANONYMOUS_ID, "Anonymous")
		);
	}

	@MethodSource("getNameParams")
	@ParameterizedTest
	void getName(int id, String name) {
		var principal = new WebPrincipal(id);
		assertThat(principal.getName()).isEqualTo(name);
		assertThat(principal.getId()).isEqualTo(id);
		assertThat(principal.toString()).isEqualTo(name);
	}

	private static Stream<Arguments> isXXXParams() {
		return Stream.of(
				Arguments.of(WebPrincipal.SYSTEM_ID, false, true, false),
				Arguments.of(WebPrincipal.ADMIN_ID, false, false, true),
				Arguments.of(5, false, false, false),
				Arguments.of(WebPrincipal.ANONYMOUS_ID, true, false, false)
		);
	}

	@MethodSource("isXXXParams")
	@ParameterizedTest
	void isXXX(int id, boolean anonymous, boolean sys, boolean admin) {
		var principal = new WebPrincipal(id);
		assertThat(principal.isAnonymous()).isEqualTo(anonymous);
		assertThat(principal.isSystem()).isEqualTo(sys);
		assertThat(principal.isAdminister()).isEqualTo(admin);
	}

	private static Stream<Arguments> hasPermissionParams() {
		return Stream.of(
				Arguments.of(WebPrincipal.SYSTEM_ID, true),
				Arguments.of(WebPrincipal.ADMIN_ID, true),
				Arguments.of(5, false),
				Arguments.of(WebPrincipal.ANONYMOUS_ID, false)
		);
	}

	@MethodSource("hasPermissionParams")
	@ParameterizedTest
	void hasPermission(int id, boolean expect) {
		var principal = new WebPrincipal(id);
		assertThat(principal.hasPermission("Any")).isEqualTo(expect);
	}

	@Test
	void equalsAngHashcode() {
		var sys = new WebPrincipal(WebPrincipal.SYSTEM_ID);
		var sys2 = new WebPrincipal(WebPrincipal.SYSTEM_ID);
		var u66 = new WebPrincipal(66);

		assertThat(sys2).isEqualTo(sys);
		assertThat(sys2).isNotEqualTo(u66);

		var set = new HashSet<WebPrincipal>();
		set.add(sys);
		set.add(sys2);
		set.add(u66);
		assertThat(set).isEqualTo(Set.of(sys, u66));
	}
}
