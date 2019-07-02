package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.article.model.ArticleContentBase;

import javax.validation.constraints.NotEmpty;

/**
 * 从草稿创建文章的请求。
 *
 * 【更新】发表后是否删除草稿应当由前端决定，比如显示一个选项让用户自己决定是否保留草稿。
 */
@Getter
@Setter
final class PublishInput extends ArticleContentBase {

	private int draftId;

	/** 发表成功后是否删除草稿 */
	private boolean destroy;

	private int category;

	@NotEmpty
	private String urlTitle;
}
