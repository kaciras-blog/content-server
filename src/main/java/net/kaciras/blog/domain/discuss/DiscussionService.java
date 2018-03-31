package net.kaciras.blog.domain.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.DeletedState;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.ConfigBind;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public final class DiscussionService {

	private final DiscussRepository discussRepository;

	private Authenticator authenticator;
	private boolean allowAnonymous;

	@ConfigBind("discussion.allowAnonymous")
	public void setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
	}

	@Qualifier("discussionAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}


	/* - - - - - - - - - - - - - - - 业务方法 - - - - - - - - - - - - - - - - -  */

	private void verifyQuery(DiscussionQuery query) {
		if (query.getDeletedState() != DeletedState.FALSE) {
			Integer quid = query.getUserId();
			Integer loginedUser = SecurtyContext.getCurrentUser();
			if (quid == null || !Objects.equals(quid, loginedUser)) {
				authenticator.require("POWER_QUERY");
			}
		}
	}

	public Discussion getOne(int id) {
		Discussion result = discussRepository.get(id);
		if (result.isDeleted() && !Objects.equals(result.getUserId(), SecurtyContext.getCurrentUser())) {
			authenticator.require("POWER_QUERY");
		}
		return result;
	}

	public List<Discussion> getList(DiscussionQuery query) {
		verifyQuery(query);
		return discussRepository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		verifyQuery(query);
		return discussRepository.size(query);
	}

	public int add(Discussion discussion) {
		Integer loginedUserId = SecurtyContext.getCurrentUser();
		int uid = 0;
		if (loginedUserId == null) {
			if (!allowAnonymous)
				throw new PermissionException(); // 401更好？
		} else {
			authenticator.require("ADD");
			uid = loginedUserId;
		}

		discussion.setUserId(uid);
		discussRepository.add(discussion);
		return discussion.getId();
	}

	public void voteUp(int id) {
		int userId = SecurtyContext.getRequiredCurrentUser();
		discussRepository.get(id).addVote(userId);
	}

	public void revokeVote(int id) {
		int userId = SecurtyContext.getRequiredCurrentUser();
		discussRepository.get(id).removeVote(userId);
	}

	public void delete(int id) {
		Discussion discussion = discussRepository.get(id);
		if (!Objects.equals(discussion.getUserId(), SecurtyContext.getCurrentUser())) {
			authenticator.require("POWER_MODIFY");
		}
		discussion.delete();
	}

	public void restore(int id) {
		authenticator.require("POWER_MODIFY"); //恢复无论是不是自己都需要权限
		discussRepository.get(id).restore();
	}
}
