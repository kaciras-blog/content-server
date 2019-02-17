package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Configurable
public final class ReplyList {

	@Autowired
	private DiscussionDAO dao;

	private final Discussion parent;

	/**
	 * 添加新的回复到楼中楼列表中。
	 *
	 * @param reply 回复
	 * @return 生成的回复ID
	 */
	public long add(Discussion reply) {
		reply.setParent(parent.getId());
		reply.setObjectId(parent.getObjectId());
		reply.setType(parent.getType());
		reply.setFloor(parent.getFloor());
		dao.insert(reply);
		return reply.getId();
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
