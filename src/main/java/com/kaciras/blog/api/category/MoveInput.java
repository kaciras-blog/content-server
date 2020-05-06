package com.kaciras.blog.api.category;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class MoveInput {

	private final int id;

	private final int parent;

	private final boolean treeMode;
}
