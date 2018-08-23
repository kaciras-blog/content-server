package net.kaciras.blog.config;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class FunctionConvertor<T> implements Converter<T> {

	private final Class<T> clazz;

	private final Function<String, T> function;

	@Override
	public boolean matchs(Class type) {
		return clazz == type;
	}

	@Override
	public T convert(String source, Class type) {
		return function.apply(source);
	}
}
