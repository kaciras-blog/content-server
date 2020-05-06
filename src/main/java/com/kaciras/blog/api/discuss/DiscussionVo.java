package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.user.UserVo;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public final class DiscussionVo {

	private int id;
	private int objectId;
	private int type;
	private int parent;
	private int floor;

	private UserVo user;
	private String nickname;
	private String content;

	private boolean deleted;
	private int voteCount;

	private Instant time;

	// 下面是非共有字段

	private boolean voted;
	private List<DiscussionVo> replies;
	private int replyCount;

	/** 被评论的对象，该字段不限制类型，不同的对象可能类型也不同 */
	private Object target;
}
