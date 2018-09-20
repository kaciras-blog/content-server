package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.ConfigBind;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public final class DiscussionService {

	private final DiscussRepository repository;

	private Authenticator authenticator;
	private boolean allowAnonymous;

	@ConfigBind("discuss.anonymous")
	public void setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
	}

	@Qualifier("DiscussionAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}


	/* - - - - - - - - - - - - - - - 业务方法 - - - - - - - - - - - - - - - - -  */

	private void verifyQuery(DiscussionQuery query) {
		if (query.getDeletion() != DeletedState.FALSE && SecurtyContext.isNotUser(query.getUserId())) {
			authenticator.require("POWER_QUERY");
		}
	}

	public Discussion getOne(int id) {
		var result = repository.get(id);
		if (result.isDeleted() && SecurtyContext.isNotUser(result.getUserId())) {
			authenticator.require("POWER_QUERY");
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

	public int add(int articleId, String content) {
		var dis = Discussion.create(requireAddedUser(), content);
		dis.setArticleId(articleId);
		repository.add(dis);
		return dis.getId();
	}

	public int addReply(int disId, String content) {
		var reply = Discussion.create(requireAddedUser(), content);
		repository.get(disId).getReplyList().add(reply);
		return reply.getId();
	}

	private int requireAddedUser() {
		var loginedUserId = SecurtyContext.getCurrentUser();

		if (loginedUserId == null) {
			if (!allowAnonymous)
				throw new PermissionException();
			return 0;
		}
		authenticator.require("ADD");
		return loginedUserId;
	}

	private void checkContent(String content) {

	}

	public void voteUp(int id) {
		var userId = SecurtyContext.getRequiredCurrentUser();
		repository.get(id).addVote(userId);
	}

	public void revokeVote(int id) {
		var userId = SecurtyContext.getRequiredCurrentUser();
		repository.get(id).removeVote(userId);
	}

	public void delete(int id) {
		var discussion = repository.get(id);
		if (SecurtyContext.isNotUser(discussion.getUserId())) {
			authenticator.require("POWER_MODIFY");
		}
		discussion.delete();
	}

	public void restore(int id) {
		authenticator.require("POWER_MODIFY"); //恢复无论是不是自己都需要权限
		repository.get(id).restore();
	}
}
