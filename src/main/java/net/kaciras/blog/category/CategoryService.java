package net.kaciras.blog.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.Authenticator;
import net.kaciras.blog.AuthenticatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository repository;

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

	public int add(Category category, int parent) {
		authenticator.require("MODIFY");
		return repository.add(category, parent);
	}

	public void update(Category category) {
		authenticator.require("MODIFY");
		repository.update(category);
	}

	public void delete(int id) {
		authenticator.require("MODIFY");
		repository.remove(id);
	}

	public List<Category> getPath(int id) {
		if (id == 0) {
			return Collections.emptyList();
		}
		return repository.get(id).getPath();
	}
}
