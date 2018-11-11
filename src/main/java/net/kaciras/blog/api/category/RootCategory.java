package net.kaciras.blog.api.category;

import net.kaciras.blog.infrastructure.exception.ResourceStateException;

import java.util.Collections;
import java.util.List;

/**
 * 根分类，覆盖了分类的一些方法，防止这些操作被错误地用到顶级分类上。
 */
final class RootCategory extends Category {

	@Override
	public Category getParent() {
		return null;
	}

	@Override
	public int getLevel() {
		return 0;
	}

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
