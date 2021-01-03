package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 评论所在的主题，表示评论集合所在的板块。
 * 比如博客里的文章、论坛里的帖子、GitHub 里的 issue。
 */
@RequiredArgsConstructor
@Getter
public final class Topic {

	/**
	 * 主题的名字，可用于显示在前端。
	 */
	private final String name;

	/**
	 * 主题的地址，在这个地址里可以浏览其评论。
	 */
	private final String url;
}
