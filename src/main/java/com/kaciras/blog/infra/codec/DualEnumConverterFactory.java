package com.kaciras.blog.infra.codec;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * 同时支持枚举名和索引转换为枚举的转换器，如果字符串可以被解析为整数，则使用索引，否则使用名字。
 *
 * @see org.springframework.core.convert.support.StringToEnumConverterFactory
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class DualEnumConverterFactory implements ConverterFactory<String, Enum> {

	@Override
	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		return source -> convert(source, targetType);
	}

	private <T extends Enum> T convert(String source, Class<T> enumType) {
		if (source.isEmpty()) {
			return null;
		}
		try {
			var i = Integer.parseInt(source);
			return enumType.getEnumConstants()[i];
		} catch (NumberFormatException e) {
			return (T) Enum.valueOf(enumType, source.trim());
		}
	}
}
