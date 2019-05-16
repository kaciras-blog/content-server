package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.api.config.BindConfig;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

	private final DiscussRepository repository;
	private final ArticleRepository articleRepository;
	private final DiscussionDAO dao;

	@BindConfig("discussion")
	private DiscussionOptions options;

	/** 检查用户是否能够评论 */
	private void checkDiscussable() {
		if (!options.isEnabled()) {
			throw new PermissionException();
		}
		if (!options.isAllowAnonymous()) {
			SecurityContext.requireLogin();
		}
	}

	public List<Discussion> getList(DiscussionQuery query) {
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		return repository.size(query);
	}

	public long add(AddRequest request, InetAddress address) {
		checkDiscussable();
		articleRepository.get(request.getObjectId()); // 检查文章是否存在
		Discussion dis;

		if (request.getParent() != 0) {
			dis = repository.get(request.getParent()).createReply(SecurityContext.getUserId(), request.getContent());
		} else {
			dis = Discussion.create(request.getObjectId(), SecurityContext.getUserId(), 0, request.getContent());
		}
		dis.setAddress(address);
		dis.setState(options.isReview() ? DiscussionState.Moderation : DiscussionState.Visible);

		repository.add(dis);
		return dis.getId();
	}

	public void voteUp(int id, InetAddress address) {
		repository.get(id).getVoterList().add(address);
	}

	public void revokeVote(int id, InetAddress address) {
		repository.get(id).getVoterList().remove(address);
	}

	/**
	 * 批量更新评论的状态。
	 * TODO:
	 * 因为单个更新属于批量更新的特例，所以它也使用了这个方法。但如果遇到了需要在JAVA代码
	 * 里鉴权等逻辑，则无法用一条SQL直接更新，还是得一个个UPDATE，目前也没找到更好的方法。
	 *
	 * @param patchMap 更新记录
	 */
	@Transactional
	public void batchUpdate(PatchMap patchMap) {
		patchMap.ids.forEach(id -> dao.updateState(id, patchMap.state));
	}
}
