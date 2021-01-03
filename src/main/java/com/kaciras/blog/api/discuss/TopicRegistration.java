package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.article.ArticleRepository;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 主题管理中心，处理主题相关的逻辑。
 * 主题这一层抽象用于将评论功能嵌入到不同的系统，就像 Disqus 那样。
 * <p>
 * 目前仅有两个类型所以就只有一个方法，如果要支持扩展就有得写了。
 */
@RequiredArgsConstructor
@Component
public class TopicRegistration {

	private final ArticleRepository articleRepository;

	@Value("${app.origin}")
	@Setter
	private String origin;

	/**
	 * 获取评论所在的主题。
	 *
	 * @param discussion 评论
	 * @return 主题
	 */
	public Topic get(Discussion discussion) {
		return get(discussion.getType(), discussion.getObjectId());
	}

	/**
	 * 查询一个评论主题的基本信息，如果主题不存在则抛出异常。
	 *
	 * <h2>ID的起始值</h2>
	 * 作为数字类型的默认值，零应当保留作为特殊意义比如未设置或无效，在某些情况下可以避免不必要的麻烦。
	 * 所以这里的 type 和 objectId 都是从一开始。
	 *
	 * <h2>URL 的问题</h2>
	 * 页面的组织是前端的事情，按理说不应该在这里构造 URL，但如果 URL 随着评论一起提交，
	 * 则可能被伪造，后端要验证的话还是需要知道前端路由。
	 * 下一版如果使用Node全栈，也许可以复用路由代码来确定 URL。
	 *
	 * @param type     类型
	 * @param objectId 对象ID
	 * @return 主题对象
	 * @throws RequestArgumentException 如果主题不存在，这里为了省事没用 NotFound 异常
	 */
	@NonNull
	public Topic get(int type, int objectId) {
		if (type == 1) {
			var article = articleRepository.findById(objectId);
			var url = String.format("%s/article/%d/%s", origin, article.getId(), article.getUrlTitle());
			return new Topic(article.getTitle(), url);
		}
		if (type == 2) {
			switch (objectId) {
				case 1:
					return new Topic("关于 - 博主", origin + "/about/me");
				case 2:
					return new Topic("关于 - 友链", origin + "/about/friends");
			}
		}
		throw new RequestArgumentException("被评论的对象不存在");
	}
}
