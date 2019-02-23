package net.kaciras.blog.api;

import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.Article;
import net.kaciras.blog.api.article.ArticleRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ArticleDataFetcher {

	private final ArticleRepository repository;

	public Article getById(DataFetchingEnvironment environment) {
		return repository.get(environment.getArgument("id")).orElse(null);
	}
}
