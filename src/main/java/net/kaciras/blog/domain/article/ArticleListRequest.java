package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.domain.ListSelectRequest;

@Getter
@Setter
public class ArticleListRequest extends ListSelectRequest {

	private int userId;

	private boolean showDeleted;

	private Integer category;
}
