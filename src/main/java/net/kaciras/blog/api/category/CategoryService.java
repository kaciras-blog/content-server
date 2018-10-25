package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository repository;
	private final CategoryMapper mapper;

	@Transactional
	public void move(int id, int parent, boolean treeMode) {
		SecurityContext.require("CHANGE_RELATION");
		var category = repository.get(id);
		var newParent = repository.get(parent);

		if (treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}
	}

	public Category getById(int id) {
		return repository.get(id);
	}

	public List<Category> getChildren(int id) {
		return repository.get(id).getChildren();
	}

	public int add(CategoryAttributes attributes, int parent) {
		SecurityContext.require("MODIFY");
		return repository.add(mapper.toCategory(attributes), parent);
	}

	public void update(int id, CategoryAttributes attributes) {
		SecurityContext.require("MODIFY");

		var category = repository.get(id);
		mapper.update(category, attributes);
		repository.update(category);
	}

	public void delete(int id) {
		SecurityContext.require("MODIFY");
		repository.remove(id);
	}

	public List<SimpleCategoryVo> getPath(int id, int ancestor) {
		if (id == 0) {
			return Collections.emptyList();
		}
		return mapper.simpleView(repository.get(id).getPathTo(ancestor));
	}

	public Banner getBanner(int id) {
		if (id == 0) {
			return null;
		}
		return getBanner(repository.get(id));
	}

	public Banner getBanner(Category category) {
		try {
			if (category.getBackground() != null) {
				return new Banner(category.getBackground(), category.getTheme());
			}
			for (var parent : category.getPath()) {
				if (parent.getBackground() != null) {
					return new Banner(parent.getBackground(), category.getTheme());
				}
			}
		} catch (ResourceNotFoundException e) {
			logger.warn("Category[id={}] not found when get banner");
		}
		return null;
	}
}
