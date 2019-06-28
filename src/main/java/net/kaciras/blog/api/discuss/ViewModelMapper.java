package net.kaciras.blog.api.discuss;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.article.model.ArticleManager;
import net.kaciras.blog.api.user.UserManager;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 *                                      加上是否点赞过
 *  （用户ID转为对象）                  加上回复第一页
 * 1.包含最基本字段的回复 -------------2----------------> 3.对外显示的评论
 *                          加上回复数 |
 *                                     +----------------> 4.控制台显示
 *                                     加上评论目标的信息
 *
 * 注：MapStruct真TM垃圾，@IterableMapping(qualifiedByName = "@Named") 根本没用
 */
@Mapper(config = MapStructConfig.class)
abstract class ViewModelMapper {

	@Autowired
	private ArticleManager articleManager;

	@Autowired
	private UserManager userManager;

	@Autowired
	private DiscussRepository repository;

	// 4
	public final List<DiscussionVo> toLinkedView(List<Discussion> model) {
		return model.stream()
				.map(this::ConvertWithVoteExt)
				.peek(this::attachTarget)
				.collect(Collectors.toList());
	}

	public final List<DiscussionVo> toAggregatedView(List<Discussion> model, InetAddress address) {
		return model.stream().map(m -> toAggregatedView(m, address)).collect(Collectors.toList());
	}

	public final List<DiscussionVo> toReplyView(List<Discussion> model) {
		return model.stream().map(this::toReplyView).collect(Collectors.toList());
	}

	/**
	 * 对不同类型对象的评论包含一些无法统一的地方，其中之一就是评论目标的链接信息。
	 * 为了在控制台显示出评论的目标，必须带有该字段，但是`文章`和`关于页`的评论目标所包含的信息不一样。
	 * 文章包含urlTitle而关于页则没有。
	 *
	 * @param viewObject 评论视图对象
	 */
	public final void attachTarget(DiscussionVo viewObject) {
		if (viewObject.getType() == 1) {
			viewObject.setTarget("（关于页）关于博主");
		} else {
			viewObject.setTarget(articleManager.getLink(viewObject.getObjectId()));
		}
	}

	// 3
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

	// 2
	private DiscussionVo ConvertWithVoteExt(Discussion model) {
		var result = mappingProperties(model);
		result.setUser(userManager.getUser(model.getUserId()));
		result.setReplyCount(repository.size(new DiscussionQuery().setParent(model.getId())));
		return result;
	}

	// 1
	public final DiscussionVo toReplyView(Discussion model) {
		var result = mappingProperties(model);
		result.setUser(userManager.getUser(model.getUserId()));
		return result;
	}

	protected abstract DiscussionVo mappingProperties(Discussion model);
}
