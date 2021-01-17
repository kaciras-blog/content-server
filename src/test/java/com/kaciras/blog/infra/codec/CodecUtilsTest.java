package com.kaciras.blog.infra.codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CodecUtilsTest {

	@Test
	void toIPv6Bytes() throws Exception {
		var addr = InetAddress.getByName("127.0.0.5");
		var bytes = CodecUtils.toIPv6Bytes(addr);

		assertThat(addr).isInstanceOf(Inet4Address.class);
		assertThat(bytes).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xFF, 0xFF, 127, 0, 0, 5);
	}

	@Test
	void toIPv6BytesV6() throws Exception {
		var addr = InetAddress.getByName("9876:5432::10FF");
		var bytes = CodecUtils.toIPv6Bytes(addr);
		assertThat(bytes).containsExactly(0x98, 0x76, 0x54, 0x32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x10, 0xFF);
	}

	@Test
	void indexOfBytes() {
		var text = "CodecUtilsTest.indexOfBytes";
		var subText = "sTest.i";

		var i = CodecUtils.indexOfBytes(text.getBytes(), subText.getBytes(), 0);
		assertThat(i).isEqualTo(text.indexOf(subText));

		var k = CodecUtils.indexOfBytes(text.getBytes(), subText.getBytes(), 12);
		assertThat(k).isEqualTo(-1);
	}

	@Test
	void hexDigit() {
		"0123456789abcdefABCDEF".chars().forEach(c -> assertThat(CodecUtils.isHexDigit((char) c)).isTrue());
	}

	@ValueSource(strings = {
			"０１２３４５６７８９ａｂｃｄｅｆＡＢＣＤＥＦ",
			"\r\n~!@@#$^&%*() /: @gG` 测下符号和边界值",
	})
	@ParameterizedTest
	void nonHexDigit(String text) {
		text.chars().forEach(c -> assertThat(CodecUtils.isHexDigit((char) c)).isFalse());
	}

	@Test
	void decodeHexBadText() {
		assertThatThrownBy(() -> CodecUtils.decodeHex("A")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CodecUtils.decodeHex("T0")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CodecUtils.decodeHex("0T")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void decodeHexEmpty() {
		assertThat(CodecUtils.decodeHex("")).isEmpty();

		var buffer = new byte[0];
		var rv = CodecUtils.decodeHex(buffer, 0, "");

		assertThat(rv).isEmpty();
		assertThat(rv).isSameAs(buffer);
	}

	@Test
	void encodeDecodeHexCharArrayRandom() {
		var random = new Random();

		for (int i = 5; i > 0; i--) {
			var data = new byte[random.nextInt(10000) + 1];
			random.nextBytes(data);

			var encodedChars = CodecUtils.encodeHex(data);
			byte[] decodedBytes = CodecUtils.decodeHex(encodedChars);
			assertThat(decodedBytes).containsExactly(data);
		}
	}

	@CsvSource({
			"Hello World, 48656c6c6f20576f726c64",
			"测试字符串, e6b58be8af95e5ad97e7aca6e4b8b2",
	})
	@ParameterizedTest
	void encodeHex(String text, String hex) {
		var bytes = text.getBytes(StandardCharsets.UTF_8);
		assertThat(CodecUtils.encodeHex(bytes)).isEqualTo(hex);
	}

	@Test
	void encodeHexEmpty() {
		assertThat(CodecUtils.encodeHex(new byte[0])).isEqualTo("");

		var helloWorld = "Hello World".getBytes();
		assertThat(CodecUtils.encodeHex(helloWorld, 7, 0)).isEqualTo("");
	}
}
