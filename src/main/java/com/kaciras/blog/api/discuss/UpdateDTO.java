package com.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
final class UpdateDTO {

	public final List<Integer> ids;
	public final DiscussionState state;
}
