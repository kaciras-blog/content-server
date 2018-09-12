package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@Data
@Configurable
public class Discussion {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiscussionDAO dao;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private VoteDAO voteDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MessageClient messageClient;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;
	private int userId;

	private int articleId;
	private int floor;

	private int parent;

	private String content;

	private int voteCount;

	private LocalDateTime time;
	private boolean deleted;

	/**
	 * 点赞，一个用户只能点赞一次
	 *
	 * @param userId 点赞用户的id
	 */
	void addVote(int userId) {
		try {
			voteDAO.insertRecord(id, userId);
			voteCount++;
			dao.increaseVote(id);
		} catch (DataIntegrityViolationException ex) {
			throw new ResourceStateException();
		}
	}

	/**
	 * 取消点赞，只有先点赞了才能取消
	 *
	 * @param userId 点赞用户的id
	 */
	void removeVote(int userId) {
		try {
			DBUtils.checkEffective(voteDAO.deleteRecord(id, userId));
			voteCount--;
			dao.descreaseVote(id);
		} catch (DataIntegrityViolationException ex) {
			throw new ResourceStateException();
		}
	}

	// delete和restore这两个方法我认为应该放在Domain Object里
	// 因为它们是对deleted属性的修改，而不是真正的删除

	void delete() {
		dao.updateDeleted(id, true);
	}

	void restore() {
		dao.updateDeleted(id, false);
	}
}