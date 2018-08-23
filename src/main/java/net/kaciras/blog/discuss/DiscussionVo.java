package net.kaciras.blog.discuss;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.kaciras.blog.user.UserVo;

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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;
}
