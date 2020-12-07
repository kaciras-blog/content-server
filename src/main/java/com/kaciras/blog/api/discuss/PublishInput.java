package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PublishInput {

	private final int objectId;
	private final int type;

	private final int parent;

	private final int score;

	@Length(max = 16)
	private final String nickname;

	@NotBlank
	private final String content;
}
