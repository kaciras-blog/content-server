package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.user.UserVo;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class DiscussionVo {

	private int id;

	private int objectId;
	private int type;

	private int parent;

	private int channelFloor;
	private int replyFloor;

	private UserVo user;

	private String nickname;
	private String content;
	private Instant time;

	private boolean deleted;

	// ========== 下面是可选的聚合属性 ==========

	private DiscussChannel channel;

	private ListQueryView<DiscussionVo> replies;
}
