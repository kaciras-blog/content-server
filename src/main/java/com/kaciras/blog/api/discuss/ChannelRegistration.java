package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.article.ArticleRepository;
import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 频道的管理中心，处理评论频道相关的操作。
 *
 * 目前仅有两个类型所以就只有一个方法，如果要支持扩展就有得写了。
 */
@RequiredArgsConstructor
@Component
public final class ChannelRegistration {

	private final ArticleRepository articleRepository;

	@Value("${app.origin}")
	private String origin;

	/**
	 * 查询一个评论频道的基本信息，如果频道不存在则抛出异常。
	 * <p>
	 * 【URL 的问题】
	 * 页面的组织是前端的事情，按理说不应该在这里构造 URL，但如果 URL 随着评论一起提交，
	 * 则可能被伪造，后端要验证的话还是需要知道前端路由。
	 * 下一版如果使用Node全栈，也许可以复用路由代码来确定 URL。
	 *
	 * @param type     类型
	 * @param objectId 对象ID
	 * @return 频道对象
	 * @throws ResourceNotFoundException 如果频道不存在
	 */
	@NonNull
	public DiscussChannel getChannel(int type, int objectId) {
		if (type == 0) {
			var article = articleRepository.findById(objectId);
			var url = String.format("%s/article/%d/%s", origin, article.getId(), article.getUrlTitle());
			return new DiscussChannel(article.getTitle(), url);
		}
		if (type == 1) {
			return new DiscussChannel("关于 - 博主", origin + "/about/blogger");
		}
		throw new ResourceNotFoundException("被评论的对象不存在");
	}
}
