package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.user.UserVo;

import java.time.Instant;
import java.util.List;

public final class DiscussionVo {

	public int id;

	public int objectId;
	public int type;
	public int parent;
	public int floor;

	public int nestId;
	public int nestFloor;
	public int nestSize;

	public UserVo user;
	public String nickname;
	public String content;
	public Instant time;
	public DiscussionState state;

	// ========== 下面是可选的聚合属性 ==========

	public List<Integer> replies;
}
