package com.kaciras.blog.api.discuss;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 在后台批量修改评论状态请求的请求体。
 */
@AllArgsConstructor
final class UpdateDTO {

	public final List<Integer> ids;
	public final DiscussionState state;
}
