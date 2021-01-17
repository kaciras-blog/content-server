package com.kaciras.blog.infra.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public final class HttpURIValidator implements ConstraintValidator<HttpURI, URI> {

	@Override
	public boolean isValid(URI value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		var scheme = value.getScheme();
		if (scheme == null || value.getHost() == null) {
			return false;
		}
		return scheme.equals("https") || scheme.equals("http");
	}
}
