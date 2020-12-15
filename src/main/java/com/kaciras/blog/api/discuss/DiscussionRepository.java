package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
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
	 * 添加一条评论，此方法会在评论对象中设置自动生成的 id, time 以及 floor。
	 * 因为评论的楼层是连续的，所以新评论的楼层就是已有评论的数量 + 1。
	 * <p>
	 * 使用了串行级别的事务，因为楼层的确定需要获取评论数，存在幻读的可能。
	 * <p>
	 * 【楼层号从1开始】
	 * 虽然咱码农的世界里编号都是从0开始的，但从1开始更通用些。
	 * 先前是从0开始的，可以使用一句 SQL 更新：UPDATE discussion SET `floor`=`floor`+1
	 *
	 * @param discussion 评论对象
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(@NonNull Discussion discussion) {
		var parent = discussion.getParent();

		if (parent != 0) {
			if (discussion.getState() == DiscussionState.Visible) {
				dao.addReplyCount(parent, 1);
			}
			// parent.getReplies() 返回的是可见的回复数，这里需要的是总数
			discussion.setFloor(dao.countByParent(parent) + 1);
		} else {
			discussion.setFloor(dao.countTopLevel(discussion) + 1);
		}

		discussion.setTime(clock.instant());
		dao.insert(discussion);
	}

	public int count(@NonNull DiscussionQuery query) {
		return dao.count(query);
	}

	public Optional<Discussion> get(int id) {
		return dao.selectById(id);
	}

	public List<Discussion> findAll(@NonNull DiscussionQuery query) {
		var pageable = query.getPageable();

		// 检查排序字段，如果在 SQLProvider 里检查错误会被包装
		if (pageable != null && pageable.getSort().isSorted()) {
			var column = Misc.getFirst(pageable.getSort()).getProperty();
			switch (column) {
				case "id":
				case "reply":
					break;
				default:
					throw new RequestArgumentException("不支持的排序：" + column);
			}
		}

		return dao.selectList(query);
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
			dao.addReplyCount(discussion.getParent(), -1);
		} else if (!ov && nv) {
			dao.addReplyCount(discussion.getParent(), 1);
		}
	}
}
