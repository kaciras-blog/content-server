package net.kaciras.blog.api.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;

@Setter
@Getter
public class TestBindingConfig implements ValidatedConfig{

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

	private static final class SubConfig {
		private boolean boolValue;
	}
}
