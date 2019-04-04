package net.kaciras.blog.api.discuss;

import lombok.*;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.DataTooBigException;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "userId", "objectId"})
@Data
@Configurable
public final class Discussion {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiscussionDAO dao;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private long id;

	private int objectId;
	private int type;

	private int floor;
	private long parent;

	private int userId;
	private String content;

	private LocalDateTime time;
	private boolean deleted;

	private int voteCount;

	public ReplyList getReplyList() {
		if (parent != 0) {
			throw new ResourceNotFoundException("楼中楼不能再包含楼中楼了");
		}
		return new ReplyList(this);
	}

	public VoterList getVoterList() {
		return new VoterList(this.id);
	}

	public void updateDeletion(boolean value) {
		DBUtils.checkEffective(dao.updateDeleted(id, value));
	}

	/**
	 * 创建一个评论，该方法会检查评论内容是否合法。
	 *
	 * @param userId  评论者ID
	 * @param content 评论内容
	 * @return 评论对象
	 */
	public static Discussion create(int userId, String content) {
		if (content == null || content.length() == 0) {
			throw new RequestArgumentException("评论内容不能为空");
		}
		if (content.length() > 64 * 40) {
			throw new DataTooBigException("评论内容过长，请分多次发表");
		}
		var dis = new Discussion();
		dis.setUserId(userId);
		dis.setContent(content);
		return dis;
	}
}
