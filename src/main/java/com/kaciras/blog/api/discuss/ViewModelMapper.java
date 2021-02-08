package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVO;
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
	public abstract Discussion fromInput(PublishDTO input);

	/**
	 * 从评论创建通知对象，其中内容字段将截断到 200 字以内。
	 *
	 * @param value 评论
	 * @param topic 主题
	 * @return 通知对象
	 */
	@Mapping(target = "title", source = "topic.name")
	@Mapping(target = "preview", source = "value")
	public abstract DiscussionActivity toActivity(Discussion value, Topic topic);

	final String contentPreview(Discussion value) {
		var c = value.getContent();
		return c.length() > 200 ? c.substring(0, 200) : c;
	}

	/**
	 * 把评论模型转换为视图对象，复制对应的字段。
	 *
	 * @param source 评论模型对象
	 * @return 评论视图对象
	 */
	@Mapping(target = "user", source = "source")
	abstract DiscussionVO toViewObject(Discussion source);

	UserVO userFromDiscussion(Discussion discussion) {
		return userManager.getUser(discussion.getUserId());
	}
}
