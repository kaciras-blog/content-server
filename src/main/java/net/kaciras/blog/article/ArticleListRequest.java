package net.kaciras.blog.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.ListSelectRequest;

@Getter
@Setter
public class ArticleListRequest extends ListSelectRequest {

	private int userId;

	private Integer category;
}
