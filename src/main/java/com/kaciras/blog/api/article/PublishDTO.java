package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 从草稿创建文章的请求，属性仍由前端传递。
 */
@AllArgsConstructor
final class PublishDTO {

	/** 文章的标题是必须要有的啦 */
	@NotBlank
	public final String title;

	public final ImageReference cover;
	public final List<String> keywords;
	public final String summary;
	public final String content;

	/** 如果懒得填则使用空字符串 */
	@NotNull
	public final String urlTitle;

	public final int category;

	public final int draftId;

	/** 发表成功后是否删除草稿 */
	public final boolean destroy;
}
