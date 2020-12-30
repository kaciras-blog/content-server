package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapStructConfig.class)
abstract class ViewModelMapper {

	@Autowired
	private UserManager userManager;

	/**
	 * 从发布请求创建一个评论对象，复制对应的字段。
	 *
	 * @param input 请求
	 * @return 评论对象
	 */
	public abstract Discussion fromInput(PublishInput input);

	protected UserVo userFromDiscussion(Discussion discussion) {
		return userManager.getUser(discussion.getUserId());
	}

	/**
	 * 把评论模型转换为视图对象，复制对应的字段。
	 *
	 * @param source 评论模型对象
	 * @return 评论视图对象
	 */
	@Mapping(target = "user", source = "source")
	protected abstract DiscussionVo toViewObject(Discussion source);
}
