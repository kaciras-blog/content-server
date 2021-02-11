package com.kaciras.blog.api.discuss;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
final class UpdateDTO {

	public final List<Integer> ids;
	public final DiscussionState state;
}
