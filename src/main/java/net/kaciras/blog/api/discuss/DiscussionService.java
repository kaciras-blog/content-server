package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
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

	@Value("${discuss.anonymous}")
	private boolean allowAnonymous;

	@Value("${discuss.disable}")
	private boolean disabled;

	@Value("${discuss.review}")
	private boolean review;

	public List<Discussion> getList(DiscussionQuery query) {
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		return repository.size(query);
	}

	public long add(int objectId, int type, String content, InetAddress address) {
		checkAddedUser();

		var discussion = Discussion.create(SecurityContext.getUserId(), content);
		discussion.setObjectId(objectId);
		discussion.setType(type);
		discussion.setAddress(address);
		discussion.setState(review ? DiscussionState.Pending : DiscussionState.Visible);

		repository.add(discussion);
		return discussion.getId();
	}

	public long addReply(long disId, String content, InetAddress address) {
		checkAddedUser();

		var reply = Discussion.create(SecurityContext.getUserId(), content);
		reply.setAddress(address);
		reply.setState(review ? DiscussionState.Pending : DiscussionState.Visible);

		repository.get(disId).getReplyList().add(reply);
		return reply.getId();
	}

	private void checkAddedUser() {
		if (disabled) {
			throw new PermissionException();
		}
		if (!allowAnonymous) {
			SecurityContext.requireLogin();
		}
	}
}
