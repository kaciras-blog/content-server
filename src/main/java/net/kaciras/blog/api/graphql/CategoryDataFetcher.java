package net.kaciras.blog.api.graphql;

import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.category.Category;
import net.kaciras.blog.api.category.CategoryManager;
import net.kaciras.blog.api.category.CategoryRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CategoryDataFetcher {

	private final CategoryRepository repository;
	private final CategoryManager manager;

	public Category getById(DataFetchingEnvironment environment) {
		return repository.get(environment.getArgument("id"));
	}

	public String getBanner(DataFetchingEnvironment environment) {
		Category category = environment.getSource();
		return manager.getBanner(category).toString();
	}
}
