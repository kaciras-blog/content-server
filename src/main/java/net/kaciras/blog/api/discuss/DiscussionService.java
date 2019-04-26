package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.api.config.ConfigBind;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

	private final DiscussRepository repository;
	private final ArticleRepository articleRepository;

	@Value("${discuss.anonymous}")
	private boolean allowAnonymous;

	@Value("${discuss.disable}")
	private boolean disabled;

	@ConfigBind("discuss.review")
	private boolean review;

	// 检查用户是否能够评论
	private void checkAddedUser() {
		if (disabled) {
			throw new PermissionException();
		}
		if (!allowAnonymous) {
			SecurityContext.requireLogin();
		}
	}

	public List<Discussion> getList(DiscussionQuery query) {
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		return repository.size(query);
	}

	public long add(int objectId, String content, InetAddress address) {
		checkAddedUser();
		articleRepository.get(objectId); // 检查文章是否存在

		var discussion = Discussion.create(objectId, SecurityContext.getUserId(), 0, content);
		discussion.setAddress(address);
		discussion.setState(review ? DiscussionState.Pending : DiscussionState.Visible);

		repository.add(discussion);
		return discussion.getId();
	}

	public long addReply(long discussionId, String content, InetAddress address) {
		checkAddedUser();
		var parent = repository.get(discussionId);

		var reply = parent.createReply(SecurityContext.getUserId(), content);
		reply.setAddress(address);
		reply.setState(review ? DiscussionState.Pending : DiscussionState.Visible);

		repository.add(reply);
		return reply.getId();
	}

	public void update(int id, PatchMap patchMap) {
		if (patchMap.state != null) {
			repository.get(id).updateState(patchMap.state);
		}
	}

	public void voteUp(int id, int userId) {
		SecurityContext.requireLogin();
		repository.get(id).getVoterList().add(userId);
	}

	public void revokeVote(int id, int userId) {
		SecurityContext.requireLogin();
		repository.get(id).getVoterList().remove(userId);
	}
}
