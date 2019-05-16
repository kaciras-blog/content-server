package net.kaciras.blog.api.discuss;

/** 评论的状态。请勿更改顺序，数据库依赖次序作为值 */
public enum DiscussionState {

	Visible, // 正常显示
	Deleted, // 已删除
	Moderation, // 等待审核
}
