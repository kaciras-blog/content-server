package com.kaciras.blog.api.category;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
final class MoveDTO {

	public final int id;

	public final int parent;

	public final boolean treeMode;
}
