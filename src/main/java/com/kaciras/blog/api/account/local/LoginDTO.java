package com.kaciras.blog.api.account.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
final class LoginDTO {

	@Length(min = 1, max = 16)
	public final String name;

	@Length(min = 8, max = 128)
	public final String password;

	public final boolean remember;
}
