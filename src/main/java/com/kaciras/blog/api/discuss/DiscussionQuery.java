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

	private Integer nestId;

	@NotNull
	private DiscussionState state = DiscussionState.VISIBLE;

	private Pageable pageable;

	// ======== 下面的字段不在 SQL 中使用 ========

	private boolean includeTopic;

	/**
	 * 在结果中包含每个评论的子评论数量，默认为零。
	 */
	@Max(5)
	private int childCount;

	/**
	 * 是否在结果中加入每个评论的父评论。
	 */
	private boolean includeParent;
}
