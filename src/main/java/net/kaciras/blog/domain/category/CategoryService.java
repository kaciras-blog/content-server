package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.CategoryRemovedEvent;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final MessageClient messageClient;

	public void moveTree(int id, int parent, boolean treeMode) {
		SecurtyContext.checkAccept("CategoryService", "MODIFY");
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
		SecurtyContext.checkAccept("CategoryService", "MODIFY");
		return categoryRepository.add(category, parent);
	}

	public void update(Category category) {
		SecurtyContext.checkAccept("CategoryService", "MODIFY");
		categoryRepository.update(category);
	}

	public void delete(int id) {
		SecurtyContext.checkAccept("CategoryService", "MODIFY");
		categoryRepository.delete(id);
		CategoryRemovedEvent event = new CategoryRemovedEvent();
		event.setId(id);
		messageClient.send(event);
	}

	public List<Category> getPath(int id) {
		if (id == 0) {
			return Collections.emptyList();
		}
		return categoryRepository.get(id).getPath();
	}

	public List<Category> getPath(int id, int ancestor) {
		return categoryRepository.get(id).pathTo(ancestor);
	}
}
