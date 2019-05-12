package net.kaciras.blog.api.discuss;

import lombok.*;
import net.kaciras.blog.infrastructure.exception.DataTooBigException;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.net.InetAddress;
import java.time.LocalDateTime;

@NoArgsConstructor
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

	private int id;

	// 创建时就需要的字段
	private int objectId;
	private int userId;
	private int parent;
	private String content;

	private LocalDateTime time;
	private InetAddress address;
	private int floor;

	// 可变字段
	private DiscussionState state;
	private int voteCount;

	private Discussion(int objectId, int userId, int parent, String content) {
		this.objectId = objectId;
		this.userId = userId;
		this.parent = parent;
		this.content = content;
	}

	public VoterList getVoterList() {
		return new VoterList(this.id);
	}

	public Discussion createReply(int userId, String content) {
		return create(objectId, userId, id, content);
	}

	/**
	 * 创建一个评论，该方法会检查评论内容是否合法。
	 *
	 * @param userId  评论者ID
	 * @param content 评论内容
	 * @return 评论对象
	 */
	public static Discussion create(int objectId, int userId, int parent, String content) {
		if (content == null || content.length() == 0) {
			throw new RequestArgumentException("评论内容不能为空");
		}
		if (content.length() > 64 * 40) {
			throw new DataTooBigException("评论内容过长");
		}
		return new Discussion(objectId, userId, parent, content);
	}
}
