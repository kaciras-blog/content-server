package com.kaciras.blog.infra.codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ImageReferenceTest {

	@ValueSource(strings = {
			"A53RqvF4tbcDUqwe783hje3rmkjsH.png",
			"ZBLARqvF4/+cDUmPkjsH.png",
			"ZBLARqvF4-_cDUmPkjsH",
			"..\\ZBLARqvF4-_cDUmPkjsH.png",
			"",
			"ZBLARqvF4-_cDUmPkjsH.abc",
	})
	@ParameterizedTest
	void parseInvalid(String value) {
		assertThatThrownBy(() -> ImageReference.parse(value)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void parseHash() {
		var name = "ZBLARqvF4-_cDUmPkjsH.png";
		var parse = ImageReference.parse(name);

		assertThat(parse.getType()).isEqualTo(ImageType.PNG);
		assertThat(parse.toString()).isEqualTo(name);
	}

	@Test
	void testEquality() {
		var imageA = new ImageReference("ZBLARqvF4-_cDUmPkjsH", ImageType.WEBP);
		var imageB = ImageReference.parse("ZBLARqvF4-_cDUmPkjsH.webp");

		assertThat(imageA).isEqualTo(imageB);
		assertThat(imageA.hashCode()).isEqualTo(imageB.hashCode());
	}
}
