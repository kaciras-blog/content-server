package net.kaciras.blog.api.article;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ArticleLink {

	private final int id;
	private final String urlTitle;
	private final String title;
}
