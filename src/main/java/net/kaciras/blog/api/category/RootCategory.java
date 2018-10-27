package net.kaciras.blog.api.category;

import net.kaciras.blog.infrastructure.exception.ResourceStateException;

import java.util.Collections;
import java.util.List;

public final class RootCategory extends Category {

	@Override
	public List<Category> getPathTo(int ancestor) {
		return Collections.emptyList();
	}

	@Override
	public void moveTo(Category target) {
		throw new ResourceStateException("根分类不支持此操作");
	}

	@Override
	public void moveTreeTo(Category target) {
		throw new ResourceStateException("根分类不支持此操作");
	}
}
