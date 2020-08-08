package com.kaciras.blog.api.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class DiscussionActivity {

	/** 评论所在的页面地址和标题 */
	private String url;
	private String title;

	private int floor;
	private int parentFloor;

	/** 内容预览 */
	private String preview;

	/** 评论时间 */
	private Instant time;
}
