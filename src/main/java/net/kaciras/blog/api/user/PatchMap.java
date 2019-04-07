package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kaciras.blog.infrastructure.codec.ImageReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PatchMap {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	private final String name;

	@NotNull
	private final ImageReference head;
}
