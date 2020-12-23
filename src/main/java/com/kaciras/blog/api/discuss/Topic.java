package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 一个主题表示评论所在的板块，比如博客里的文章；论坛里的帖子；聊天室里的房间；GitHub 里的 issue。
 * <p>
 * 虽然感觉主题这个词不太准确，但英语太烂也想不出更好的。
 */
@RequiredArgsConstructor
@Getter
public final class Topic {

	/**
	 * 主题的名字，可用于显示在前端。
	 */
	public final String name;

	/**
	 * 主题的地址，在这个地址里可以浏览其评论。
	 */
	public final String url;
}
