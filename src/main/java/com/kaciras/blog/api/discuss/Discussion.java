package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.InetAddress;
import java.time.Instant;

/**
 * 评论对象，无论是顶层评论还是楼中楼都是这个。
 * <p>
 * 【关于点赞功能】
 * 评论曾经有点赞功能但后来移除了，理由有这么几点：
 * <ol>
 *     <li>
 *     点赞只有在人数足够的情况下才有意义，而博客站很难有这么多的评论者，这让它成为了鸡肋。
 *     据我观察大多数博客都没有此功能，贴吧也没有。
 *     </li>
 *     <li>
 *     评论系统以匿名为主，只能以 IP 来鉴别独立访问者，但 IP 容易变导致一个人可以重复点赞。
 *     </li>
 * </ol>
 */
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "userId", "objectId"})
@Data
public final class Discussion {

	/** 每条评论都有唯一的ID */
	private int id;

	private int objectId;
	private int type;

	private int userId;
	private int parent;
	private int floor;

	private String nickname;
	private String content;

	private int score;

	private DiscussionState state;

	/** 提交评论的时间 */
	private Instant time;

	/** 发送评论的IP，用于批量查找垃圾评论 */
	private InetAddress address;

	/** 回复（楼中楼）总数 */
	private int reply;

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
