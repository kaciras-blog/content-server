package com.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;

/**
 * ArticleController 中 PATCH 方法的参数。
 */
@RequiredArgsConstructor
final class UpdateDTO {

	public final Integer category;
	public final Boolean deletion;
	public final String urlTitle;
}
