package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
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
	 * 添加一条评论，此方法会在评论对象中设置自动生成的 id 以及 floor。
	 * 因为评论的楼层是连续的，所以新评论的楼层就是已有评论的数量 + 1。
	 * <p>
	 * 【楼层号从1开始】
	 * 虽然咱码农的世界里编号都是从0开始的，但从1开始更通用些。
	 * 先前是从0开始的，可以使用一句 SQL 更新：UPDATE discussion SET `floor`=`floor`+1
	 *
	 * @param discussion 评论对象
	 */
	@Transactional
	public void add(@NonNull Discussion discussion) {
		if (discussion.getParent() != 0) {
			var parent = dao.selectById(discussion.getParent()).orElseThrow(RequestArgumentException::new);

			if (discussion.getState() == DiscussionState.Visible) {
				dao.addRepliesColumn(parent.getId(), 1);
			}

			/*
			 * 当前的数据库设计中评论的ID是全局的，
			 */
			if (discussion.getObjectId() != parent.getObjectId()
					|| discussion.getType() != parent.getType()) {
				throw new RequestArgumentException("与父评论的频道不同");
			}

			// parent.getReplies() 返回的是可见的回复数，这里需要的是总数
			discussion.setFloor(dao.countByParent(parent.getId()) + 1);
		} else {
			discussion.setFloor(dao.countTopLevel(discussion.getType(), discussion.getObjectId()) + 1);
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

		// 检查排序字段
		if (pageable != null && pageable.getSort().isSorted()) {
			var column = Misc.getFirst(pageable.getSort()).getProperty();
			switch (column) {
				case "reply":
				case "id":
				case "score":
					break;
				default:
					throw new RequestArgumentException("不支持的排序：" + column);
			}
		}

		return dao.selectList(query);
	}

	/**
	 * 批量更新评论的状态。
	 * TODO:
	 * 因为单个更新属于批量更新的特例，所以它也使用了这个方法。但如果遇到了需要在JAVA代码
	 * 里鉴权等逻辑，则无法用一条SQL直接更新，还是得一个个UPDATE，目前也没找到更好的方法。
	 *
	 * @param ids   ID列表
	 * @param state 新状态
	 */
	@Transactional
	public void updateAll(List<Integer> ids, DiscussionState state) {
		var increment = state == DiscussionState.Visible ? 1 : -1;
		for (var id : ids) {
			var discussion = dao.selectById(id).orElseThrow(RequestArgumentException::new);
			dao.updateState(id, state);
			dao.addRepliesColumn(discussion.getParent(), increment);
		}
	}
}
