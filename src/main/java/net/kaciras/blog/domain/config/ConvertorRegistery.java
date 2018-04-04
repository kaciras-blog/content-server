package net.kaciras.blog.domain.config;

import org.springframework.stereotype.Component;

import java.util.*;

@SuppressWarnings("unchecked")
@Component
final class ConvertorRegistery {

	private final Collection<Converter> converters;

	private final Map<String, Class> primitives;

	ConvertorRegistery() {
		converters = List.of(
				new EnumConvertor(),
				new FunctionConvertor<>(String.class, v -> v),
				new FunctionConvertor<>(Integer.TYPE, Integer::parseInt),
				new FunctionConvertor<>(Integer.class, Integer::parseInt),
				new FunctionConvertor<>(Boolean.TYPE, Boolean::parseBoolean),
				new FunctionConvertor<>(Boolean.class, Boolean::parseBoolean),
				new FunctionConvertor<>(Double.class, Double::parseDouble),
				new FunctionConvertor<>(Double.TYPE, Double::parseDouble),
				new FunctionConvertor<>(Long.TYPE, Long::parseLong),
				new FunctionConvertor<>(Long.class, Long::parseLong),
				new FunctionConvertor<>(Float.TYPE, Float::parseFloat),
				new FunctionConvertor<>(Float.class, Float::parseFloat)
		);
		primitives = Map.of(
				"byte", Byte.TYPE,
				"short", Short.TYPE,
				"int", Integer.TYPE,
				"long", Long.TYPE,
				"bool", Boolean.TYPE,
				"float", Float.TYPE,
				"double", Double.TYPE,
				"void", Void.TYPE
		);
	}

	boolean checkType(String typeName, String value) {
		try{
			convert(typeName, value);
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	<T> T convert(String typeName, String value) {
		try {
			Class primitiveType = primitives.get(typeName);
			return convert(primitiveType == null ? Class.forName(typeName) : primitiveType, value);
		} catch (ClassNotFoundException e) {
			throw new ClassCastException("无法将配置项转换为所需的类型");
		}
	}

	<T> T convert(Class clazz, String value) {
		Optional<Converter> converter = converters.stream()
				.filter(conv -> conv.matchs(clazz))
				.findFirst();
		if (!converter.isPresent()) {
			throw new ClassCastException("无法将配置项转换为所需的类型");
		}
		return (T) converter.get().convert(value, clazz);
	}
}
