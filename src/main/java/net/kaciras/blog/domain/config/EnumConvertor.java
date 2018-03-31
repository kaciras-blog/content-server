package net.kaciras.blog.domain.config;

public class EnumConvertor implements Converter {

	@Override
	public boolean matchs(Class type) {
		return type.isEnum();
	}

	@Override
	public Object convert(String source, Class type) {
		return Enum.valueOf(type, source);
	}
}
