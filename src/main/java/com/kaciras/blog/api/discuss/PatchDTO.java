package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
final class PatchDTO {

	public final List<Integer> ids;

	public final DiscussionState state;
}
