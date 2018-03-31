package net.kaciras.blog.domain.discuss;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
class DiscussRepository {

	private final DiscussionDAO discussionDAO;

	public DiscussRepository(DiscussionDAO discussionDAO) {
		this.discussionDAO = discussionDAO;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void add(Discussion discuz) {
		int lastFloor = discussionDAO.selectLastFloor(discuz.getPostId());
		discuz.setFloor(lastFloor + 1);
		discussionDAO.insert(discuz);
	}

	public Discussion get(int id) {
		return discussionDAO.selectById(id);
	}

	public List<Discussion> findAll(DiscussionQuery query) {
		return discussionDAO.selectList(query);
	}

	public int size(DiscussionQuery query) {
		return discussionDAO.selectCount(query);
	}
}
