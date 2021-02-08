package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;

import java.time.Instant;
import java.util.List;

// TODO: 跟ArticleVo越来越像了
final class PreviewVO {

	public int id;
	public String urlTitle;

	public String title;
	public ImageReference cover;
	public List<String> keywords;
	public String summary;

	public String content;

	public Instant create;
	public Instant update;

	public int viewCount;
	public int discussionCount;
	public boolean deleted;

	public List<CategoryNode> categories;
}
