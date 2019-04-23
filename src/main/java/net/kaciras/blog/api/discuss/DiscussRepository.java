package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
class DiscussRepository {

	private final DiscussionDAO dao;

	/**
	 * 添加一条评论，此方法会在评论对象中设置自动生成的ID。
	 * 使用了串行级别的事务，因为楼层的确定需要获取评论数，存在幻读的可能。
	 *
	 * @param dis 评论对象
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(@NonNull Discussion dis) {
		var count = dao.selectCountByObject(dis.getObjectId(), dis.getType());
		dis.setFloor(count); // 评论的楼层是连续的，新评论的楼层就是已有评论的数量
		dao.insert(dis);
	}

	@NonNull
	public Discussion get(long id) {
		return dao.selectById(id).orElseThrow(ResourceNotFoundException::new);
	}

	public List<Discussion> findAll(@NonNull DiscussionQuery query) {
		if (query.getPageable().getPageSize() > 30) {
			throw new RequestArgumentException("单次查询数量太多");
		}
		return dao.selectList(query);
	}

	public int size(@NonNull DiscussionQuery query) {
		return dao.selectCount(query);
	}
}
