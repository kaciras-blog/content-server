package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
		return select(PageRequest.of(start, size));
	}

	public List<Discussion> select(Pageable pageable) {
		var query = DiscussionQuery.byParent(parent.getId());
		query.setPageable(pageable);
		return dao.selectList(query);
	}
}
