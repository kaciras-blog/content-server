package com.kaciras.blog.api.discuss;

import lombok.Getter;

@Getter
final class DiscussionOptions {

	/** 是否开启评论功能 */
	private boolean enabled = true;

	/** 是否允许不登录评论 */
	private boolean allowAnonymous = true;

	/** 评论是否需要审核后才显示 */
	private boolean moderation;
}
