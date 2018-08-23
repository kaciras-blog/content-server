package net.kaciras.blog.discuss;

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

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(Discussion discuz) {
		int lastFloor = discussionDAO.selectLastFloor(discuz.getArticleId());
		discuz.setFloor(lastFloor + 1);
		discussionDAO.insert(discuz);
	}

	public Discussion get(int id) {
		return discussionDAO.selectById(id);
	}

	public List<Discussion> findAll(DiscussionQuery query) {
		if (query.getCount() > 30) {
			throw new RequestArgumentException("单次查询数量太多");
		}
		if(query.getArticleId() == null
				&& query.getParent() == null
				&& query.getUserId() == null) {
			throw new RequestArgumentException("请指定查询条件");
		}
		return discussionDAO.selectList(query);
	}

	public int size(DiscussionQuery query) {
		if(query.getArticleId() == null
				&& query.getParent() == null
				&& query.getUserId() == null) {
			throw new RequestArgumentException("请指定查询条件");
		}
		return discussionDAO.selectCount(query);
	}
}
