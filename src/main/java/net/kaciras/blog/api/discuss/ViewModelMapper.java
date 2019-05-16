package net.kaciras.blog.api.discuss;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.article.ArticleManager;
import net.kaciras.blog.api.user.UserManager;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
abstract class ViewModelMapper {

	@Autowired
	private ArticleManager articleManager;

	@Autowired
	private UserManager userManager;

	@Autowired
	private DiscussRepository repository;

	// MapStruct真TM垃圾，@IterableMapping(qualifiedByName = "@Named") 根本没用
	public final List<DiscussionVo> toLinkedView(List<Discussion> model) {
		return model.stream().map(this::toLinkedView).collect(Collectors.toList());
	}
	public final List<DiscussionVo> toAggregatedView(List<Discussion> model, InetAddress address) {
		return model.stream().map(m -> toAggregatedView(m, address)).collect(Collectors.toList());
	}
	public final List<DiscussionVo> toReplyView(List<Discussion> model) {
		return model.stream().map(this::toReplyView).collect(Collectors.toList());
	}

	public final DiscussionVo toLinkedView(Discussion model) {
		var result = ConvertWithVoteExt(model);
		result.setTarget(getTarget(model));
		return result;
	}

	public final DiscussionVo toAggregatedView(Discussion model, InetAddress address) {
		var result = ConvertWithVoteExt(model);

		var replies = repository.findAll(new DiscussionQuery()
				.setParent(model.getId())
				.setPageable(PageRequest.of(0, 5)));

		result.setReplies(replies.stream()
				.map(this::toReplyView)
				.collect(Collectors.toList()));

		result.setVoted(model.getVoterList().contains(address));
		return result;
	}

	private DiscussionVo ConvertWithVoteExt(Discussion model) {
		var result = toReplyView(model);
		result.setReplyCount(repository.size(new DiscussionQuery().setParent(model.getId())));
		return result;
	}

	public final DiscussionVo toReplyView(Discussion model) {
		var result = mappingProperties(model);
		result.setUser(userManager.getUser(model.getUserId()));
		return result;
	}

	private Object getTarget(Discussion discussion) {
		if (discussion.getType() == 1) {
			return "关于博主";
		}
		return articleManager.getLink(discussion.getObjectId());
	}

	abstract DiscussionVo mappingProperties(Discussion model);
}
