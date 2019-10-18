package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.api.config.BindConfig;
import net.kaciras.blog.infra.exception.PermissionException;
import net.kaciras.blog.infra.exception.RequestArgumentException;
import net.kaciras.blog.infra.principal.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscussionService {

	private final DiscussionRepository repository;
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
		if (query.getPageable().getPageSize() > 20) {
			throw new RequestArgumentException("单次查询数量太多");
		}
		return repository.findAll(query);
	}

	public int count(DiscussionQuery query) {
		return repository.count(query);
	}

	public Discussion add(PublishInput input, InetAddress address) {
		checkDiscussable();

		var user = SecurityContext.getUserId();
		Discussion discussion;

		if (input.getParent() == 0) {
			// 检查文章是否存在。目前仅有两个类型而且区别逻辑较少，所以暂时没有做抽象
			if (input.getType() == 0) {
				articleRepository.get(input.getObjectId());
			}
			discussion = Discussion.create(input.getObjectId(), input.getType(), user, input.getContent());
		} else {
			discussion = repository.get(input.getParent()).createReply(user, input.getContent());
		}

		discussion.setNickname(input.getNickname());
		discussion.setAddress(address);
		discussion.setState(options.isModeration() ? DiscussionState.Moderation : DiscussionState.Visible);

		repository.add(discussion);
		return discussion;
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
	 * @param patchInput 更新记录
	 */
	@Transactional
	public void batchUpdate(PatchInput patchInput) {
		patchInput.ids.forEach(id -> dao.updateState(id, patchInput.state));
	}
}
