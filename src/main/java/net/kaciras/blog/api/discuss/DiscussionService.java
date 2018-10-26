package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public final class DiscussionService {

	private final DiscussRepository repository;

	@Value("${discuss.anonymous}")
	private boolean allowAnonymous;

	@Value("${discuss.disable}")
	private boolean disabled;


	/* - - - - - - - - - - - - - - - 业务方法 - - - - - - - - - - - - - - - - -  */

	private void verifyQuery(DiscussionQuery query) {
		if (query.getDeletion() != DeletedState.FALSE && SecurityContext.isNot(query.getUserId())) {
			SecurityContext.require("POWER_QUERY");
		}
	}

	public Discussion getOne(long id) {
		var result = repository.get(id);
		if (result.isDeleted() && SecurityContext.isNot(result.getUserId())) {
			SecurityContext.require("POWER_QUERY");
		}
		return result;
	}

	public List<Discussion> getList(DiscussionQuery query) {
		verifyQuery(query);
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		verifyQuery(query);
		return repository.size(query);
	}

	public long add(int objectId, int type, String content) {
		var dis = Discussion.create(requireAddedUser(), content);
		dis.setObjectId(objectId);
		dis.setType(type);
		repository.add(dis);
		return dis.getId();
	}

	public long addReply(long disId, String content) {
		var reply = Discussion.create(requireAddedUser(), content);
		repository.get(disId).getReplyList().add(reply);
		return reply.getId();
	}

	private int requireAddedUser() {
		var discusser = SecurityContext.getUserId();
		if (disabled) {
			throw new PermissionException();
		}
		if (discusser == 0) {
			if (!allowAnonymous)
				throw new PermissionException();
			return 0;
		}
		return discusser;
	}
}
