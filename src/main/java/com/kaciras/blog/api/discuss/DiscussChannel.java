package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 一个频道表示评论所在的板块，比如某个文章或特定的页面。
 */
@RequiredArgsConstructor
@Getter
public final class DiscussChannel {

	/**
	 * 频道的名字，可用于显示在前端。
	 */
	public final String name;

	/**
	 * 频道的地址，在这个地址里可以浏览其评论。
	 */
	public final String url;
}
