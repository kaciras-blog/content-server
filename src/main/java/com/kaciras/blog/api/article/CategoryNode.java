package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;

// 虽然在分类领域中也可能需要类似的视图，但不应看做是同一对象
final class CategoryNode {

	public int id;
	public ImageReference cover;
	public String name;
}
