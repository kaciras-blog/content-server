package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public void moveTree(int id, int parent, boolean treeMode) {
		authenticator.require("CHANGE_RELATION");
		Category category = categoryRepository.get(id);
		if (treeMode)
			category.moveTreeTo(parent);
		else
			category.moveTo(parent);
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
