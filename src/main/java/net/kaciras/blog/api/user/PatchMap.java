package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
final class PatchMap {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	public final String name;

	@NotNull
	public final ImageRefrence head;
}
