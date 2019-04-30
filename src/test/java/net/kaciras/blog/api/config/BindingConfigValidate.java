package net.kaciras.blog.api.config;

import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;

public class BindingConfigValidate implements ValidatedConfig {

	@NotEmpty
	private String text = "123";

	private int smaller;
	private int bigger = smaller + 1;

	@Override
	public boolean validate() throws ValidationException {
		return bigger > smaller;
	}
}
