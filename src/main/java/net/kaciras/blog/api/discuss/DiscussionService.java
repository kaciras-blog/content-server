package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.ConfigBind;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.infrastructure.exception.DataTooBigException;
import net.kaciras.blog.infrastructure.exception.LegallyProhibitedException;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.TextUtils;
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

	public int add(Discussion discussion) {
		var loginedUserId = SecurtyContext.getCurrentUser();
		var uid = 0;

		if (loginedUserId == null) {
			if (!allowAnonymous)
				throw new PermissionException();
		} else {
			authenticator.require("ADD");
			uid = loginedUserId;
		}

		if(TextUtils.getHeight(discussion.getContent(), 40) > 64) {
			throw new DataTooBigException("评论内容过长，请分多次发表");
		}
		if(TextUtils.isDanger(discussion.getContent())) {
			throw new LegallyProhibitedException("评论包含不和谐内容");
		}

		discussion.setUserId(uid);
		repository.add(discussion);
		return discussion.getId();
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
