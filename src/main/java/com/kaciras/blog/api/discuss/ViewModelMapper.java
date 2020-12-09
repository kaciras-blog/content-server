package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.user.UserManager;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注：MapStruct真TM垃圾，@IterableMapping(qualifiedByName = "@Named") 根本没用
 */
@Mapper(config = MapStructConfig.class)
abstract class ViewModelMapper {

	@Autowired
	private DiscussionRepository repository;

	@Autowired
	private UserManager userManager;

	/**
	 * 从发布请求创建一个评论对象，对应的字段将被设置。
	 *
	 * @param input 请求
	 * @return 评论对象
	 */
	public abstract Discussion fromInput(PublishInput input);

	public final List<DiscussionVo> toAggregatedView(List<Discussion> model, int replySize) {
		return model.stream().map(m -> toAggregatedView(m, replySize)).collect(Collectors.toList());
	}

	public final DiscussionVo toAggregatedView(Discussion model, int replySize) {
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
