package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.time.Instant;

/**
 * 评论对象，无论是顶层评论还是楼中楼都是这个。
 * <p/>
 * 【点赞功能】
 * 评论曾经有点赞功能但后来移除了，理由有这么几点：
 * <ol>
 *     <li>
 *     点赞只有在人数足够的情况下才有意义，而博客站很难有这么多的评论者，这让它成为了鸡肋。
 *     据我观察大多数博客都没有此功能，贴吧也没有。
 *     </li>
 *     <li>
 *     评论系统以匿名为主，只能以 IP 来鉴别独立访问者，但 IP 容易变导致可以重复点赞。
 *     </li>
 * </ol>
 * <p/>
 * 【热度排序功能】
 * 目前还没想好怎么设计指标，而且访问量少的情况下没有意义，先不做。
 */
@NoArgsConstructor
@Getter
@Setter
@ToString(of = {"id", "type", "objectId"})
public final class Discussion {

	/**
	 * 每条评论都有一个全局唯一的ID。
	 */
	private int id;

	private int type;
	private int objectId;

	private int userId;

	/**
	 * 父评论的ID，如果没有则为0.
	 */
	private int parent;

	/**
	 * 评论的楼层号，不同的被评论对象的楼层号是独立的。
	 */
	private int floor;

	/**
	 * 昵称用于匿名下区分不同的评论者，由前端随意填写。
	 * 该字段可以为 null，但不能为空白字符串，长度限制由其它地方决定。
	 */
	private String nickname;

	/**
	 * 评论的内容，长度限制由其它地方决定。
	 */
	private String content;

	/**
	 * 评论当前的状态。
	 */
	private DiscussionState state;

	/**
	 * 提交评论的时间。
	 */
	private Instant time;

	/**
	 * 发送评论的IP，用于批量查找垃圾评论
	 */
	private InetAddress address;

	/**
	 * 可见的回复总数，不包括删除和审核中的。
	 */
	private int replyCount;
}
