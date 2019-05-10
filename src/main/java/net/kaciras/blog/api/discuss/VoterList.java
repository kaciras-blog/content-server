package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.DataIntegrityViolationException;

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
	 * 点赞，一个用户只能点赞一次
	 *
	 * @param userId 点赞用户的id
	 * @return 如果成功则为true，若是已经点赞过或出现其他错误则返回false。
	 */
	public boolean add(int userId) {
		try {
			voteDAO.insertRecord(discussion, userId);
			voteDAO.increaseVote(discussion);
			return true;
		} catch (DataIntegrityViolationException ex) {
			return false;
		}
	}

	/**
	 * 取消点赞，只有先点赞了才能取消
	 *
	 * @param userId 点赞用户的id
	 * @return 如果成功则为true，若是用户还未点赞过则返回false。
	 */
	public boolean remove(int userId) {
		if (voteDAO.deleteRecord(discussion, userId) > 0) {
			voteDAO.decreaseVote(discussion);
			return true;
		}
		return false;
	}

	/**
	 * 指定用户是否点赞过该评论。
	 *
	 * @param userId 用户ID
	 * @return 如果点赞过了返回true
	 */
	public boolean contains(int userId) {
		return Utils.nullableBool(voteDAO.contains(discussion, userId));
	}
}
