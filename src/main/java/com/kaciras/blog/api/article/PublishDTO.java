package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 从草稿创建文章的请求。
 *
 * 【更新】发表后是否删除草稿应当由前端决定，比如显示一个选项让用户自己决定是否保留草稿。
 */
final class PublishDTO {

	public String title;
	public ImageReference cover;
	public List<String> keywords;
	public String summary;
	public String content;

	@NotEmpty
	public String urlTitle;

	public int category;

	public int draftId;

	/** 发表成功后是否删除草稿 */
	public boolean destroy;
}
