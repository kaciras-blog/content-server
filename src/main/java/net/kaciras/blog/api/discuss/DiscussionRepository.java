package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infra.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
class DiscussionRepository {

	private final DiscussionDAO dao;

	/**
	 * 添加一条评论，此方法会在评论对象中设置自动生成的 id 以及 floor。
	 * 因为评论的楼层是连续的，所以新评论的楼层就是已有评论的数量。
	 *
	 * 使用了串行级别的事务，因为楼层的确定需要获取评论数，存在幻读的可能。
	 *
	 * @param discussion 评论对象
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(@NonNull Discussion discussion) {
		var count = dao.selectTopLevelCount(discussion.getObjectId(), discussion.getType());
		discussion.setFloor(count);
		dao.insert(discussion);
	}

	@NonNull
	public Discussion get(int id) {
		return dao.selectById(id).orElseThrow(ResourceNotFoundException::new);
	}

	public List<Discussion> findAll(@NonNull DiscussionQuery query) {
		return dao.selectList(query);
	}

	public int count(@NonNull DiscussionQuery query) {
		return dao.selectCount(query);
	}
}
