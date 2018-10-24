package net.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class PatchMap {

	private Integer category;
	private Boolean deletion;
}
