package com.kaciras.blog.api.notification;

import java.time.Instant;

public final class DiscussionActivity {

	/** 评论所在的页面地址和标题 */
	public String url;
	public String title;

	public int floor;
	public int parentFloor;

	/** 内容预览 */
	public String preview;

	/** 评论时间 */
	public Instant time;
}
