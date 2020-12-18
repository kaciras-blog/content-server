package com.kaciras.blog.api.discuss;

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
	 * 从发布请求创建一个评论对象，复制对应的字段。
	 *
	 * @param input 请求
	 * @return 评论对象
	 */
	public abstract Discussion fromInput(PublishInput input);

	public final List<DiscussionVo> toViewObject(List<Discussion> objects, DiscussionQuery query) {
		return objects.stream()
				.map(obj -> toViewObject(obj, query))
				.collect(Collectors.toList());
	}

	public final DiscussionVo toViewObject(Discussion discussion, DiscussionQuery query) {
		var result = toViewObject(discussion);

		// 控制台里查询的，需要加上一个链接字段
		if (query.isLinked()) {
			result.setUser(userManager.getUser(discussion.getUserId()));
		}

		if (query.getReplySize() > 0) {
			var childrenQuery = new DiscussionQuery()
					.setParent(discussion.getId())
					.setPageable(PageRequest.of(0, query.getReplySize()));

			result.setReplies(repository.findAll(childrenQuery)
					.stream()
					.map(this::toViewObject)
					.collect(Collectors.toList()));
		}

		if (query.isExpandParent()) {
			var parent = repository.get(result.getParent()).orElseThrow();
			result.setReplyTo(toViewObject(parent));
		}

		return result;
	}

	protected abstract DiscussionVo toViewObject(Discussion discussion);
}
