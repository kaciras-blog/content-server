package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Accessors(chain = true)
@Getter
@Setter
public final class DiscussionQuery {

	private Integer objectId;
	private Integer type;

	private Integer userId;

	private Integer parent;

	@NotNull
	private DiscussionState state = DiscussionState.Visible;

	private Pageable pageable;

	// ======== 下面的字段不在 SQL 中使用 ========

	@Max(5)
	private int replySize;

	/**
	 *
	 * 【没法把引用的放进内容】
	 * GitHub 的 Comment 里将引用的评论作为内容的一部分并用 blockquote 标识，
	 * 这在支持双模式的设计里行不通，因为引用的内容在楼中楼的里纯属多余。
	 */
	private boolean referenceMode;
}
