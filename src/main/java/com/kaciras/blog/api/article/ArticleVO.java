package com.kaciras.blog.api.article;

import com.kaciras.blog.api.category.Banner;

import java.time.Instant;
import java.util.List;

final class ArticleVO {

	public int id;
	public String urlTitle;
	public int category;
	public boolean deleted;

	public String title;
	public List<String> keywords;
	public String summary;
	public String content;

	public Instant create;
	public Instant update;

	public int viewCount;
	public int discussionCount;

	public Banner banner;
	public ArticleLink prev;
	public ArticleLink next;
}
