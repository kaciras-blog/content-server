package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.ListSelectRequest;

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
