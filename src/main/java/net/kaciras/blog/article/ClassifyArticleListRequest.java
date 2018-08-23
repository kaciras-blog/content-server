package net.kaciras.blog.article;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassifyArticleListRequest extends ArticleListRequest {

	private int cid;
	private boolean recursive;
}
