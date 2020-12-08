package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
@ToString(of = {"id", "objectId"})
public final class Discussion {

	/** 每条评论都有唯一的ID */
	private int id;

	private int objectId;
	private int type;

	private int userId;
	private int parent;
	private int floor;

	/**
	 * 昵称用于匿名下区分不同的评论者，由前端随意填写，可以重复。
	 * 该字段可以为 null，但不能为空白字符串。
	 */
	private String nickname;

	private String content;

	private int score;

	private DiscussionState state;

	/** 提交评论的时间 */
	private Instant time;

	/** 发送评论的IP，用于批量查找垃圾评论 */
	private InetAddress address;

	/** 回复（楼中楼）总数，仅由 DAO 层设置 */
	private int reply;
}
