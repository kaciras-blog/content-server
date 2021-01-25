package com.kaciras.blog.api.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;

@Setter
@Getter
public class TestBindingConfig implements ValidatedConfig {

	private int intValue = 33;

	private ElementType enumValue = ElementType.FIELD;

	@NotNull
	private SubConfig subConfig = new SubConfig();

	private double smaller;
	private double bigger = smaller + 0.01;

	@Override
	public boolean validate() throws ValidationException {
		return bigger > smaller;
	}

	// [2019-4-30] Jackson 默认不从private字段读取属性。
	// 如果漏了 getter 会报错 - InvalidDefinitionException: No serializer found for class
	@Setter
	@Getter
	private static final class SubConfig {
		private boolean boolValue;
	}
}
