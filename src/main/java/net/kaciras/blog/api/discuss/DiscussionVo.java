package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.user.UserVo;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public final class DiscussionVo {

	private int id;
	private int objectId;
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
