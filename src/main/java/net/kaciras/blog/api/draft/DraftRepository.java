package net.kaciras.blog.api.draft;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 暂时都没加事务
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class DraftRepository {

	private final DraftDAO draftDAO;

	/**
	 * 每个用户最大能保存的草稿数，默认20
	 */
	@Setter
	private int userLimit = 20;

	/**
	 * 查询指定用户的所有草稿。
	 *
	 * @param userId 用户ID
	 * @return 草稿列表
	 */
	public List<Draft> findByUser(int userId) {
		Utils.checkPositive(userId, "userId");
		return draftDAO.selectByUser(userId);
	}

	/*
	 * 先使用一条语句检查了用户已有的草稿数量，也可以在数据库层面写触发器实现。
	 * 在这里检查，默认的事务级别下可能会出现幻读，但这并不会造成严重的影响。
	 */
	public void add(@NonNull Draft draft) {
		Utils.checkPositive(draft.getUserId(), "userId");
		var count = draftDAO.selectCountByUser(draft.getUserId());
		if (count > userLimit) {
			throw new IllegalStateException("用户的草稿数量已达上限");
		}
		draftDAO.insert(draft);
	}

	@Transactional
	@NonNull
	public Draft findById(int id) {
		Utils.checkPositive(id, "id");
		return draftDAO.selectById(id).orElseThrow(ResourceNotFoundException::new);
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
