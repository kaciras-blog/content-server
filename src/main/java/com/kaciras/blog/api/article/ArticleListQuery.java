package com.kaciras.blog.api.article;

import com.kaciras.blog.api.ListSelectRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public final class ArticleListQuery extends ListSelectRequest {

	private int userId;

	private int category;
	private boolean recursive;

	/** 是否包含文章内容 */
	// TODO: 搞复杂了，下一版必须要重新设计API
	private boolean content;
}
