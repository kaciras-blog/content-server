package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Configurable
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
		var query = new DiscussionQuery().setObjectId(parent.getObjectId()).setParent(parent.getId());
		return dao.selectCount(query);
	}

	public List<Discussion> select(DiscussionQuery query) {
		return dao.selectList(query.setParent(parent.getId()));
	}
}
