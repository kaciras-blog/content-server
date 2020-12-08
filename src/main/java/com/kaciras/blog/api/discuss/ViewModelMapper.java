package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.user.UserManager;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 加上是否点赞过
 * （用户ID转为对象）                  加上回复第一页
 * 1.包含最基本字段的回复 -------------2----------------> 3.对外显示的评论
 * 加上回复数 |
 * +----------------> 4.控制台显示
 * 加上评论目标的信息
 * <p>
 * 注：MapStruct真TM垃圾，@IterableMapping(qualifiedByName = "@Named") 根本没用
 */
@Mapper(config = MapStructConfig.class)
abstract class ViewModelMapper {

	@Autowired
	private UserManager userManager;

	@Autowired
	private DiscussionRepository repository;

	public abstract Discussion fromInput(PublishInput input);

	public final List<DiscussionVo> toAggregatedView(List<Discussion> model, InetAddress address, int replySize) {
		return model.stream().map(m -> toAggregatedView(m, address, replySize)).collect(Collectors.toList());
	}

	public final DiscussionVo toAggregatedView(Discussion model, InetAddress address, int replySize) {
		var result = toReplyView(model);

		var replies = repository.findAll(
				new DiscussionQuery()
						.setParent(model.getId())
						.setPageable(PageRequest.of(0, replySize))
		).stream()
				.map(this::toReplyView)
				.collect(Collectors.toList());

		result.setReplies(new ListQueryView<>(model.getReply(), replies));

		return result;
	}

	public final List<DiscussionVo> toReplyView(List<Discussion> model) {
		return model.stream().map(this::toReplyView).collect(Collectors.toList());
	}

	public final DiscussionVo toReplyView(Discussion model) {
		var result = mappingProperties(model);
		result.setUser(userManager.getUser(model.getUserId()));
		return result;
	}

	protected abstract DiscussionVo mappingProperties(Discussion model);
}
