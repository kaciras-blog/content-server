package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 从草稿创建文章的请求。
 *
 * 【更新】发表后是否删除草稿应当由前端决定，比如显示一个选项让用户自己决定是否保留草稿。
 */
@RequiredArgsConstructor
final class PublishDTO {

	public final String title;
	public final ImageReference cover;
	public final List<String> keywords;
	public final String summary;
	public final String content;

	@NotEmpty
	public final String urlTitle;

	public final int category;

	public final int draftId;

	/** 发表成功后是否删除草稿 */
	public final boolean destroy;
}
