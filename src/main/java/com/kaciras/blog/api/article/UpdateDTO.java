package com.kaciras.blog.api.article;

import lombok.AllArgsConstructor;

/**
 * ArticleController 中 PATCH 方法的参数。
 */
@AllArgsConstructor
final class UpdateDTO {

	public final Integer category;
	public final Boolean deletion;
	public final String urlTitle;
}
