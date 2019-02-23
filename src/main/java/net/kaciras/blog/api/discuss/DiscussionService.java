package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

	private final DiscussRepository repository;

	@Value("${discuss.anonymous}")
	private boolean allowAnonymous;

	@Value("${discuss.disable}")
	private boolean disabled;


	/* - - - - - - - - - - - - - - - 业务方法 - - - - - - - - - - - - - - - - -  */

	private void checkPermission(DiscussionQuery query) {
		if (query.getDeletion() != DeletedState.FALSE && SecurityContext.isNot(query.getUserId())) {
			SecurityContext.require("POWER_QUERY");
		}
	}

	public List<Discussion> getList(DiscussionQuery query) {
		checkPermission(query);
		if (query.getPageable().getPageSize() > 30) {
			throw new RequestArgumentException("单次查询数量太多");
		}
		if (query.isInvalid()) {
			throw new RequestArgumentException("请指定查询条件");
		}
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		checkPermission(query);
		return repository.size(query);
	}

	public long add(int objectId, int type, String content) {
		checkAddedUser();
		var dis = Discussion.create(SecurityContext.getUserId(), content);
		dis.setObjectId(objectId);
		dis.setType(type);
		repository.add(dis);
		return dis.getId();
	}

	public long addReply(long disId, String content) {
		checkAddedUser();
		var reply = Discussion.create(SecurityContext.getUserId(), content);
		return repository.get(disId)
				.map(Discussion::getReplyList)
				.orElseThrow(ResourceNotFoundException::new)
				.add(reply);
	}

	private void checkAddedUser() {
		var discusser = SecurityContext.getUserId();
		if (disabled) {
			throw new PermissionException();
		}
		if (discusser == 0 && !allowAnonymous) {
			throw new PermissionException();
		}
	}
}
