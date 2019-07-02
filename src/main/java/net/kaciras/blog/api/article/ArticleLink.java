package net.kaciras.blog.api.article;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kaciras.blog.api.article.model.Article;

@AllArgsConstructor
@Getter
public final class ArticleLink {

	private final int id;
	private final String urlTitle;
	private final String title;

	public static ArticleLink of(Article article) {
		return new ArticleLink(article.getId(), article.getUrlTitle(), article.getTitle());
	}
}
