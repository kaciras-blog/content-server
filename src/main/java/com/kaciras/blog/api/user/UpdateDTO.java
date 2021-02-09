package com.kaciras.blog.api.user;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
final class UpdateDTO {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	public final String name;

	@NotNull
	public final ImageReference avatar;
}
