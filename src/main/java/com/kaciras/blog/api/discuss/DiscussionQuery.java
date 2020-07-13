package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;

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

	private boolean linked;

	private Pageable pageable;

	private int replySize;
}
