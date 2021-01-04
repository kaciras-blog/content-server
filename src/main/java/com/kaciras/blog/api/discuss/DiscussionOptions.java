package com.kaciras.blog.api.discuss;

/**
 * 评论系统的选项，由配置服务管理可以在前端控制台里动态设置。
 *
 * <h3>设计注意</h3>
 * 开关类型的选项尽量以 false 作为默认值，避免使用时多余的取反。
 */
final class DiscussionOptions {

	/** 是否关闭评论功能 */
	public boolean disabled;

	/** 禁止匿名，只有登陆后才能评论 */
	public boolean loginRequired;

	/** 评论是否需要审核后才显示 */
	public boolean moderation;
}
