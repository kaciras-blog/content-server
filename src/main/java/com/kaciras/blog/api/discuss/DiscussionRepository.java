package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.Utils;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DiscussionRepository {

	private final DiscussionDAO dao;
	private final Clock clock;

	/**
	 * 添加一条评论，此方法会在评论对象中设置自动生成的 id, time 以及两个 floor。
	 * 因为楼层是连续的，所以新评论的楼层就是已有评论的数量 + 1。
	 * <p>
	 * 【楼层号从1开始】
	 * 虽然咱码农的世界里编号都是从0开始的，但从1开始更通用些。
	 * <p>
	 * 【事务】
	 * 使用了串行级别的事务，因为楼层的确定需要获取评论数，存在幻读的可能。
	 *
	 * @param discussion 评论对象
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(@NonNull Discussion discussion) {
		var pid = discussion.getParent();

		if (pid != 0) {
			var parent = get(pid).orElseThrow(RequestArgumentException::new);
			discussion.setType(parent.getType());
			discussion.setObjectId(parent.getObjectId());

			if (parent.getNestId() == 0) {
				discussion.setNestId(parent.getId());
			} else {
				discussion.setNestId(parent.getNestId());
			}

			if (discussion.getState() == DiscussionState.Visible) {
				dao.addNestSize(pid, 1);
			}

			// parent.getReplies() 返回的是可见的回复数，这里需要的是总数
			discussion.setNestFloor(dao.countByNest(discussion) + 1);
		} else {
			discussion.setNestFloor(dao.countTopLevel(discussion) + 1);
		}

		discussion.setTime(clock.instant());
		discussion.setFloor(dao.countByTopic(discussion) + 1);
		dao.insert(discussion);
	}

	public int count(@NonNull DiscussionQuery query) {
		return dao.count(query);
	}

	public Optional<Discussion> get(int id) {
		return dao.selectById(id);
	}

	public List<Discussion> findAll(@NonNull DiscussionQuery query) {
		try {
			return dao.selectList(query);
		} catch (UncategorizedDataAccessException e) {
			throw Utils.unwrapSQLException(e);
		}
	}

	/**
	 * 更新评论的状态。
	 *
	 * @param id    评论ID
	 * @param state 新状态
	 */
	@Transactional
	public void updateState(int id, DiscussionState state) {
		var discussion = dao.selectById(id).orElseThrow(RequestArgumentException::new);
		dao.updateState(id, state);

		// 从可见变为不可见，或反过来时需要更新父评论的回复数
		var ov = discussion.getState() == DiscussionState.Visible;
		var nv = state == DiscussionState.Visible;
		if (ov && !nv) {
			dao.addNestSize(discussion.getParent(), -1);
		} else if (!ov && nv) {
			dao.addNestSize(discussion.getParent(), 1);
		}
	}
}
