package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class AddRequest {

	@Range(min = 1)
	private final int objectId;

	private final int parent;

	@NotEmpty
	private final String content;
}
