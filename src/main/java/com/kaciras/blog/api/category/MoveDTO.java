package com.kaciras.blog.api.category;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class MoveDTO {

	public final int id;
	public final int parent;
	public final boolean treeMode;
}
