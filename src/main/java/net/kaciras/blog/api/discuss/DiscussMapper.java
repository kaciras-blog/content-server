package net.kaciras.blog.api.discuss;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.user.UserManager;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@Mapper(config = MapStructConfig.class)
abstract class DiscussMapper {

	@Autowired
	private DiscussionService discussionService;

	@Autowired
	private UserManager userManager;

	@IterableMapping(qualifiedByName = "DIS")
	public abstract List<DiscussionVo> toDiscussionView(List<Discussion> discussions);

	@Named("DIS")
	DiscussionVo convertDiscussion(Discussion model) {
		var vo = toReplyView(model);
		vo.setVoted(model.getVoterList().contains(SecurityContext.getUserId()));

		var query = new DiscussionQuery()
				.setParent(model.getId())
				.setPageable(PageRequest.of(0,5));

		vo.setReplyCount(discussionService.count(query));
		vo.setReplies(toReplyView(discussionService.getList(query)));
		return vo;
	}

	// BUG: 多个IterableMapping不能区分name？
	public List<DiscussionVo> toReplyView(List<Discussion> replies) {
		var list = new ArrayList<DiscussionVo>(replies.size());
		for (var r : replies) {
			list.add(toReplyView(r));
		}
		return list;
	}

	DiscussionVo toReplyView(Discussion model) {
		var vo = createDiscussionVo(model);
		vo.setUser(userManager.getUser(model.getUserId()));
		return vo;
	}

	abstract DiscussionVo createDiscussionVo(Discussion model);
}
