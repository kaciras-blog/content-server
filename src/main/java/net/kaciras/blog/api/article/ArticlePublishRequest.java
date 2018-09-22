package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ArticlePublishRequest extends ArticleContentBase {

	private int draftId;

	/** 如果不为null，则更改文章的分类 */
	private Integer category;
}
