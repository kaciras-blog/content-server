package com.kaciras.blog.api.account.local;

import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

@AllArgsConstructor
final class LoginDTO {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	public final String name;

	@Length(min = 8, max = 128)
	public final String password;

	public final boolean remember;
}
