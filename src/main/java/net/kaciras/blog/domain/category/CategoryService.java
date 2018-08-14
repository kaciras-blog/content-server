package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Authenticator;
import net.kaciras.blog.domain.AuthenticatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	private Authenticator authenticator;

	@Autowired
	void setAuthenticator(AuthenticatorFactory factory) {
		this.authenticator = factory.create("CATEGORY");
	}

	@Transactional
	public void moveTree(int id, int parent, boolean treeMode) {
		authenticator.require("CHANGE_RELATION");
		Category category = categoryRepository.get(id);
		Category newParent = categoryRepository.get(parent);

		if (treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}
	}

	public Category get(int id) {
		return categoryRepository.get(id);
	}

	public List<Category> getSubCategories(int id) {
		return categoryRepository.getSubCategories(id);
	}

	public int add(Category category, int parent) {
		authenticator.require("MODIFY");
		return categoryRepository.add(category, parent);
	}

	public void update(Category category) {
		authenticator.require("MODIFY");
		categoryRepository.update(category);
	}

	public void delete(int id) {
		authenticator.require("MODIFY");
		categoryRepository.remove(id);
	}

	public List<Category> getPath(int id) {
		if (id == 0) {
			return Collections.emptyList();
		}
		return categoryRepository.get(id).getPath();
	}
}
