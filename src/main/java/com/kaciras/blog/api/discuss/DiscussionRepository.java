package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 虽然在设计上评论是主题的子树，但评论对象是聚合根，所以评论仓库不应关心主题的存在，
 * 对主题的检查必须放在外部。
 */
@RequiredArgsConstructor
@Repository
public class DiscussionRepository {

	private final DiscussionDAO dao;
	private final Clock clock;

	/**
	 * 添加一条评论，只有由用户填写的字段会被使用，其它字段在成添加功后被设置。
	 *
	 * <h2>楼层号从1开始</h2>
	 * 虽然咱码农的世界里编号都是从0开始的，但从1开始更通用些。
	 *
	 * <h2>事务</h2>
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

	public List<Discussion> get(Collection<Integer> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		return dao.selectByIds(ids);
	}

	public List<Discussion> findAll(@NonNull DiscussionQuery query) {
		return dao.selectList(query);
	}

	/**
	 * 更新一条评论的状态。
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
