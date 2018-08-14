package net.kaciras.blog.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public final class DiscussionVo {

	private int id;
	private int articleId;
	private int floor;
	private int parent;

	private UserVo user;
	private String content;

	private int voteCount;
	private boolean deleted;

	private LocalDateTime time;
}
