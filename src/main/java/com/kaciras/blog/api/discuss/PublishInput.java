package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PublishInput {

	private final int objectId;
	private final int type;
	private final int parent;

	private String nickname;
	private final String content;
}
