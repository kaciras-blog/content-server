package com.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class MoveDTO {

	public final int id;
	public final int parent;
	public final boolean treeMode;
}
