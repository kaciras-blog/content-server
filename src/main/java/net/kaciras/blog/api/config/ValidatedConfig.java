package net.kaciras.blog.api.config;

import javax.validation.ValidationException;

public interface ValidatedConfig {

	boolean validate() throws ValidationException;
}
