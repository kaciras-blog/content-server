package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 领域服务，包含一些不适合放在分类对象内部的操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryManager {

	private final CategoryRepository repository;

	public Banner getBanner(int id) {
		return getBanner(repository.get(id));
	}

	/**
	 * 获取最适合指定分类的横幅大图。
	 *
	 * @param category 分类
	 * @return 横幅对象，如果从指定分类到一级分类均未设置背景则为null。
	 */
	public Banner getBanner(Category category) {
		try {
			if (category.getBackground() != null) {
				return new Banner(category.getBackground(), category.getTheme());
			}
			for (var parent : category.getPath()) {
				if (parent.getBackground() != null) {
					return new Banner(parent.getBackground(), parent.getTheme());
				}
			}
			var root = repository.get(0);
			if (root.getBackground() != null) {
				return new Banner(root.getBackground(), root.getTheme());
			}
		} catch (ResourceNotFoundException e) {
			logger.warn("Category[id={}] not found when get banner", category.getId());
		}
		return null;
	}
}
