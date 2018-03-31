package net.kaciras.blog.domain.config;

public interface Converter<T> {

	boolean matchs(Class type);

	T convert(String source, Class type);
}
