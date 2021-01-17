package com.kaciras.blog.infra.codec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ImageReferenceTest {

	@Test
	void parseHash() {
		var name = "0FC3697B8E7787B53A76738016EB9355D812005CE6CFD354A3D6DBC812345678.png";
		var parse = ImageReference.parse(name);

		assertThat(parse.getType()).isEqualTo(ImageType.PNG);
		assertThat(parse.toString()).isEqualTo(name);
	}

	@Test
	void parseInternal() {
		var name = "picture.pcx";
		var parse = ImageReference.parse(name);

		assertThat(parse.getType()).isEqualTo(ImageType.Internal);
		assertThat(parse.toString()).isEqualTo(name);
	}

	@Test
	void parseInvalidName() {
		assertThatThrownBy(() -> ImageReference.parse("../any_system_file.sys"))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> ImageReference.parse("..\\any_system_file.sys"))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> ImageReference.parse(""))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> ImageReference.parse("toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolong"))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> ImageReference.parse("0FC3697B8E7787B53A76738016EB9355D812005CE6CFD354A3D6DBC812345678.abc"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testEquality() {
		var imageA = new ImageReference("test.webp", ImageType.Internal);
		var imageB = ImageReference.parse("test.webp");

		assertThat(imageA).isEqualTo(imageB);
		assertThat(imageA.hashCode()).isEqualTo(imageB.hashCode());
	}
}
