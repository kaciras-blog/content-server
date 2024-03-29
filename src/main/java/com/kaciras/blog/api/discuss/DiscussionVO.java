package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.user.UserVO;

import java.time.Instant;
import java.util.List;

// 邮箱不显示，仅作为接收回复通知使用。
public final class DiscussionVO {

	public int id;

	public int objectId;
	public int type;
	public int parent;
	public int floor;

	public int nestId;
	public int nestFloor;
	public int nestSize;

	public UserVO user;
	public String nickname;
	public String content;
	public Instant time;
	public DiscussionState state;

	// 下面是可选的聚合属性。

	public Topic topic;
	public List<Integer> replies;
}
