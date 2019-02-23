package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository repository;

	public List<Category> getChildren(int parent) {
		return get(parent).getChildren();
	}

	public Category get(int id) {
		return repository.get(id).orElseThrow(ResourceNotFoundException::new);
	}

	@RequireAuthorize
	public int create(Category category, int parent) {
		try {
			return repository.add(category, parent);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("分类实体中存在不合法的属性值", ex);
		}
	}

	@RequireAuthorize
	public void update(Category category) {
		repository.update(category);
	}

	@RequireAuthorize
	public void delete(int id, boolean treeMode) {
		if (!treeMode) {
			repository.remove(id);
		} else {
			repository.removeTree(id);
		}
	}

	@Transactional
	@RequireAuthorize
	public void move(int id, int parent, boolean treeMode) {
		var category = get(id);
		var newParent = get(parent);

		if (treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}
	}
}
