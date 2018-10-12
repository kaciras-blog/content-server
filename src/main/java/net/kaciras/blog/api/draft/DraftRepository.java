package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 暂时都没加事务
 */
@RequiredArgsConstructor
@Repository
class DraftRepository {

	private final DraftDAO draftDAO;

	/**
	 * 每个用户最大能保存的草稿数，默认20
	 */
	@Setter
	private int userLimit = 20;

	public List<Draft> findByUser(int userId) {
		Utils.checkPositive(userId, "userId");
		return draftDAO.selectByUser(userId)
				.stream()
				.map(this::getById)
				.collect(Collectors.toList());
	}

	/*
	 * 先使用一条语句检查了用户已有的草稿数量，也可以在数据库层面写触发器实现。
	 * 在这里检查，默认的事务级别下可能会出现幻读，但这并不会造成严重的影响。
	 */
	public int add(Draft draft) {
		Utils.checkPositive(draft.getUserId(), "userId");
		var count = draftDAO.selectCountByUser(draft.getUserId());
		if (count > userLimit) {
			throw new IllegalStateException("用户的草稿数量已达上限");
		}
		draftDAO.insertAssoicate(draft);
		draftDAO.insertHistory(draft.getId(), draft);
		return draft.getId();
	}

	public Draft getById(int id) {
		Utils.checkPositive(id, "id");
		var draft = draftDAO.selectById(id);
		if (draft == null) {
			throw new ResourceNotFoundException();
		}
		return draft;
	}

	public void update(Draft draft) {
		DBUtils.checkEffective(draftDAO.update(draft));
	}

	public void clear(int userId) {
		Utils.checkPositive(userId, "userId");
		draftDAO.deleteAll(userId);
	}

	public void remove(int id) {
		Utils.checkPositive(id, "id");
		DBUtils.checkEffective(draftDAO.deleteById(id));
	}
}
