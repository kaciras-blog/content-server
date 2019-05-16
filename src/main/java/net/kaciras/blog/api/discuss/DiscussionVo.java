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
	private int type;
	private int parent;
	private int floor;

	private UserVo user;
	private String content;

	private boolean deleted;
	private int voteCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;

	// 下面是非共有字段

	private boolean voted;
	private List<DiscussionVo> replies;
	private int replyCount;

	/** 被评论的对象，该字段不限制类型，不同的对象可能类型也不同 */
	private Object target;
}
