package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@RequiredArgsConstructor
public final class ReplyList {

	@Autowired
	private DiscussionDAO dao;

	private final Discussion parent;

	public void add(Discussion reply) {
		reply.setParent(parent.getId());
		reply.setObjectId(parent.getObjectId());
		reply.setType(parent.getType());
		reply.setFloor(parent.getFloor());
		dao.insert(reply);
	}

	public int size() {
		return dao.selectCount(DiscussionQuery.byParent(parent.getId()));
	}

	public List<Discussion> select(int start, int size) {
		var query = DiscussionQuery.byParent(parent.getId());
		query.setStart(start);
		query.setCount(size);
		return dao.selectList(query);
	}
}
