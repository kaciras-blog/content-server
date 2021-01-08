package com.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PatchInput {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	private final String name;

	@NotNull
	private final ImageReference avatar;
}
