package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.AuthenticatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository repository;
	private final CategoryMapper mapper;
	private Authenticator authenticator;

	@Autowired
	void setAuthenticator(AuthenticatorFactory factory) {
		this.authenticator = factory.create("CATEGORY");
	}

	@Transactional
	public void move(int id, int parent, boolean treeMode) {
		authenticator.require("CHANGE_RELATION");
		var category = repository.get(id);
		var newParent = repository.get(parent);

		if (treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}
	}

	public Category get(int id) {
		return repository.get(id);
	}

	public List<Category> getSubCategories(int id) {
		return repository.getSubCategories(id);
	}

	public int add(CategoryAttributes attributes, int parent) {
		authenticator.require("MODIFY");
		return repository.add(mapper.toCategory(attributes), parent);
	}

	public void update(int id, CategoryAttributes attributes) {
		authenticator.require("MODIFY");
		var category = mapper.toCategory(attributes);
		category.setId(id);
		repository.update(category);
	}

	public void delete(int id) {
		authenticator.require("MODIFY");
		repository.remove(id);
	}

	public List<CategoryVo> getPath(int id) {
		if (id == 0) {
			return Collections.emptyList();
		}
		return mapper.categoryView(repository.get(id).getPath());
	}
}
