package net.kaciras.blog.facade.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public final class DiscussionVO {

	private int id;
	private int articleId;
	private int floor;
	private int parent;

	private UserVO user;

	private String content;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;

	private int voteCount;

	private boolean deleted;
}
