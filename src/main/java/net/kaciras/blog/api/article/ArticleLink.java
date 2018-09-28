package net.kaciras.blog.api.article;

import lombok.Value;

@Value
public final class ArticleLink {

	private final int id;
	private final String urlTitle;
	private final String title;
}
