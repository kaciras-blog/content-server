package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.article.model.ArticleContentBase;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
final class PublishRequest extends ArticleContentBase {

	private int draftId;

	private Integer category;

	@NotEmpty
	private String urlTitle;
}
