package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.kaciras.blog.api.user.UserVo;

import java.time.LocalDateTime;
import java.util.List;

@Data
public final class DiscussionVo {

	private long id;
	private int objectId;
	private int type;
	private int floor;
	private int parent;

	private UserVo user;
	private String content;

	private List<DiscussionVo> replies;
	private int replyCount;

	private int voteCount;
	private boolean deleted;

	private boolean voted;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;
}
