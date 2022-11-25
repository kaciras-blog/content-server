package com.kaciras.blog.api.user;

import com.kaciras.blog.infra.codec.ImageReference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;

@AllArgsConstructor
final class UpdateDTO {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	public final String name;

	public final ImageReference avatar;

	@Email
	public final String email;
}
