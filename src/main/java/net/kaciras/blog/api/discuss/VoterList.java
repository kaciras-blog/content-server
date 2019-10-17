package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infra.exception.ResourceStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.DataIntegrityViolationException;

import java.net.InetAddress;

/**
 * 评论的点赞者列表，该列表记录了所有点赞者的ID。
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Configurable
public final class VoterList {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private VoteDAO voteDAO;

	private final int discussion;

	/**
	 * 点赞，一个IP只能点赞一次
	 *
	 * @param address 点赞者的IP地址
	 * @throws ResourceStateException 若是已经点赞过或出现其他错误
	 */
	public void add(InetAddress address) {
		try {
			voteDAO.insertRecord(discussion, address);
			voteDAO.increaseVote(discussion);
		} catch (DataIntegrityViolationException ex) {
			throw new ResourceStateException();
		}
	}

	/**
	 * 取消点赞，只有先点赞了才能取消
	 *
	 * @param address 点赞者的IP地址
	 * @throws ResourceStateException 如果用户还未点赞过
	 */
	public void remove(InetAddress address) {
		if (voteDAO.deleteRecord(discussion, address) <= 0) {
			throw new ResourceStateException();
		}
		voteDAO.decreaseVote(discussion);
	}

	/**
	 * 检查指定IP是否点赞过该评论。
	 *
	 * @param address IP地址
	 * @return 如果点赞过了返回true
	 */
	public boolean contains(InetAddress address) {
		return Utils.nullableBool(voteDAO.contains(discussion, address));
	}
}
