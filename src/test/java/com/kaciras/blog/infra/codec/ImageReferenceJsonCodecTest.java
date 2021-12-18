package com.kaciras.blog.infra.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class ImageReferenceJsonCodecTest {

	private ObjectReader reader;
	private ObjectWriter writer;

	@BeforeEach
	void setUp() {
		var objectMapper = new ObjectMapper().registerModule(new ExtendsCodecModule());
		reader = objectMapper.readerFor(ImageReference.class);
		writer = objectMapper.writerFor(ImageReference.class);
	}

	@Test
	void serializeImageServer() throws Exception {
		var image = ImageReference.parse("3IeQaaHXqjt8kQ675nCT.svg");
		var json = writer.writeValueAsString(image);
		assertThat(json).isEqualTo("\"/image/3IeQaaHXqjt8kQ675nCT.svg\"");
	}

	@Test
	void deserializeImageServer() throws Exception {
		var name = "3IeQaaHXqjt8kQ675nCT.svg";
		var json = "\"/image/" + name + "\"";
		ImageReference image = reader.readValue(json);

		assertThat(image.getName()).isEqualTo("3IeQaaHXqjt8kQ675nCT");
		assertThat(image.getType()).isEqualTo(ImageType.SVG);
		assertThat(image.toString()).isEqualTo(name);
	}

	@Test
	void deserializeInvalid() {
		var invalidPrefix = "\"https://www.example.com/image/666.jpg\"";
		Assertions.assertThatThrownBy(() -> reader.readValue(invalidPrefix))
				.isInstanceOf(JsonProcessingException.class);

		var invalidName = "\"/image/file\"";
		Assertions.assertThatThrownBy(() -> reader.readValue(invalidName))
				.isInstanceOf(JsonProcessingException.class);
	}
}
