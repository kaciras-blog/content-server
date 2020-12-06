package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.net.InetAddress;
import java.time.Instant;

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
	private int type;
	private int userId;
	private int parent;
	private String content;

	private String nickname;
	private Instant time;

	/** 发送评论的IP，用于批量查找垃圾评论 */
	private InetAddress address;

	private int floor;

	// 可变字段
	private DiscussionState state;

	private Discussion(int objectId, int type, int userId, int parent, String content) {
		this.objectId = objectId;
		this.type = type;
		this.userId = userId;
		this.parent = parent;
		this.content = content;
	}

	public Discussion createReply(int userId, String content) {
		return create(objectId, type, userId, id, content);
	}

	/**
	 * 创建一个顶层评论，该方法会检查评论内容是否合法。
	 *
	 * @param objectId 评论对象ID
	 * @param type     评论对象类型
	 * @param userId   评论者ID
	 * @param content  评论内容
	 * @return 新创建的评论对象
	 */
	public static Discussion create(int objectId, int type, int userId, String content) {
		return create(objectId, type, userId, 0, content);
	}

	private static Discussion create(int objectId, int type, int userId, int parent, String content) {
		if (content == null || content.length() == 0) {
			throw new RequestArgumentException("评论内容不能为空");
		}
		return new Discussion(objectId, type, userId, parent, content);
	}
}
