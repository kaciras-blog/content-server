package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.AuthenticatorFactory;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.category.CategoryService;
import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.api.user.UserService;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleService {

	private final UserService userService;
	private final CategoryService categoryService;
	private final DiscussionService discussionService;

	private final ArticleRepository repository;
	private final ArticleMapper mapper;
	private final MessageClient messageClient;

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`]+");

	private Authenticator authenticator;

	@Autowired
	public void setAuthenticator(AuthenticatorFactory factory) {
		this.authenticator = factory.create("ARTICLE");
	}

	/**
	 * 检查用户是否有权限更改指定的的文章。
	 *
	 * @param article 文章
	 * @return 文章
	 * @throws PermissionException 如果没权限
	 */
	private Article requireModify(Article article) {
		authenticator.require("MODIFY");
		var noPerm = authenticator.reject("POWER_MODIFY");

		if (article.isDeleted() && noPerm) {
			throw new ResourceDeletedException();
		}
		if (SecurtyContext.isNotUser(article.getUserId()) && noPerm) {
			throw new PermissionException();
		}
		return article;
	}

	public Article getArticle(int id) {
		var article = repository.get(id);
		if (article.isDeleted() && authenticator.reject("SHOW_DELETED")) {
			throw new ResourceDeletedException();
		}
		article.recordView(); //增加浏览量
		return article;
	}

	public List<PreviewVo> getList(ArticleListRequest request) {
		if (request.getDeletion() != DeletedState.FALSE) {
			authenticator.require("SHOW_DELETED");
		}
		return repository.findAll(request).stream().map(this::aggregate).collect(Collectors.toList());
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	private PreviewVo aggregate(Article article) {
		var result = mapper.toPreview(article);
		result.setAuthor(userService.getUser(article.getUserId()));
		result.setDcnt(discussionService.count(DiscussionQuery.byArticle(article.getId())));
		result.setCpath(categoryService.getPath(article.getCategory()));
		return result;
	}

	public int getCountByCategories(int id) {
		return repository.getCount(id);
	}

	/**
	 * 发布一篇文章。
	 * @param request 文章内容对象。
	 * @return 生成的ID
	 */
	@Transactional
	public int publish(ArticlePublishRequest request) {
		authenticator.require("PUBLISH");

		var article = mapper.toArticle(request);
		article.setUserId(SecurtyContext.getCurrentUser());

		article.setUrlTitle(StringUtils.trimTrailingCharacter(urlKeywords
				.matcher(request.getUrlTitle()).replaceAll("-"), '-'));

		repository.add(article);
		article.updateCategory(request.getCategory());

		messageClient.send(new ArticleCreatedEvent(article.getId(), request.getDraftId(), request.getCategory()));
		return article.getId();
	}

	/**
	 * 修改文章。
	 *
	 * @param id 文章ID。
	 * @param update 更新内容。
	 */
	@Transactional
	public void update(int id, ArticlePublishRequest update) {
		var article = repository.get(id);
		requireModify(article);

		mapper.update(article, update);
		repository.update(article);

		if(update.getCategory() != null) {
			article.updateCategory(update.getCategory());
		}

		messageClient.send(new ArticleUpdatedEvent(id, update.getDraftId(), update.getCategory()));
	}

	public void changeCategory(int id, int category) {
		requireModify(repository.get(id)).updateCategory(category);
	}

	public void updateDeleteion(int id, boolean isDeleted) {
		var article = repository.get(id);
		if (SecurtyContext.isNotUser(article.getUserId())) {
			authenticator.require("POWER_MODIFY");
		} else {
			authenticator.require("PUBLISH");
		}
		article.updateDeleted(isDeleted);
	}
}
