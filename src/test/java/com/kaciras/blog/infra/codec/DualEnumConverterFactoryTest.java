package com.kaciras.blog.infra.codec;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.support.GenericConversionService;

import java.lang.annotation.ElementType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class DualEnumConverterFactoryTest {

	private final GenericConversionService service = new GenericConversionService();

	DualEnumConverterFactoryTest() {
		service.addConverterFactory(new DualEnumConverterFactory());
	}

	@Test
	void empty() {
		var result = service.convert("", ElementType.class);
		assertThat(result).isNull();
	}

	@Test
	void name() {
		var result = service.convert("FIELD", ElementType.class);
		assertThat(result).isEqualTo(ElementType.FIELD);
	}

	@Test
	void order() {
		var result = service.convert("1", ElementType.class);
		assertThat(result).isEqualTo(ElementType.FIELD);
	}

	@Test
	void invalidName() {
		assertThatThrownBy(() -> service.convert("foobar", ElementType.class))
				.isInstanceOf(ConversionFailedException.class);
	}

	@Test
	void invalidOrder() {
		assertThatThrownBy(() -> service.convert("666", ElementType.class))
				.isInstanceOf(ConversionFailedException.class);
	}
}
