package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
class DiscussRepository {

	private final DiscussionDAO discussionDAO;

	/**
	 * 添加一条评论，此方法会在评论对象中设置自动生成的ID。
	 * 使用了串行级别的事务，因为楼层的确定需要获取评论数，存在幻读的可能。
	 *
	 * @param dis 评论对象
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(Discussion dis) {
		var count = discussionDAO.selectCountByObject(dis.getObjectId(), dis.getType());
		dis.setFloor(count); // 评论的楼层是连续的，新评论的楼层就是已有评论的数量
		discussionDAO.insert(dis);
	}

	public Discussion get(long id) {
		return discussionDAO.selectById(id);
	}

	public List<Discussion> findAll(DiscussionQuery query) {
		if (query.getPageable().getPageSize() > 30) {
			throw new RequestArgumentException("单次查询数量太多");
		}
		if(query.getObjectId() == null
				&& query.getParent() == null
				&& query.getUserId() == null) {
			throw new RequestArgumentException("请指定查询条件");
		}
		return discussionDAO.selectList(query);
	}

	public int size(DiscussionQuery query) {
		if(query.getObjectId() == null
				&& query.getParent() == null
				&& query.getUserId() == null) {
			throw new RequestArgumentException("请指定查询条件");
		}
		return discussionDAO.selectCount(query);
	}
}
