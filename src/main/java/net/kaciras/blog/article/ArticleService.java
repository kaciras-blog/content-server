package net.kaciras.blog.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.Authenticator;
import net.kaciras.blog.AuthenticatorFactory;
import net.kaciras.blog.DeletedState;
import net.kaciras.blog.SecurtyContext;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleService {

	private final ArticleRepository repository;
	private final ArticleMapper articleMapper;
	private final MessageClient messageClient;

	private Authenticator authenticator;

	private List<PreviewVo> hotArticles;

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
		boolean noPerm = authenticator.reject("POWER_MODIFY");

		if (article.isDeleted() && noPerm) {
			throw new ResourceDeletedException();
		}
		if (SecurtyContext.isNotUser(article.getUserId()) && noPerm) {
			throw new PermissionException();
		}
		return article;
	}

	public List<PreviewVo> getHotArticles() {
		return hotArticles;
	}

	public Article getArticle(int id) {
		Article article = repository.get(id);
		if (article.isDeleted() && authenticator.reject("SHOW_DELETED")) {
			throw new ResourceDeletedException();
		}
		article.recordView(); //增加浏览量
		return article;
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	void updateHotsTask() {
		ArticleListRequest request = new ArticleListRequest();
		request.setDesc(true);
		request.setSort("view_count");
		request.setCount(6);
		hotArticles = articleMapper.toPreview(repository.findAll(request));
	}

	public List<Article> getList(ArticleListRequest request) {
		if (request.getDeletion() != DeletedState.FALSE) {
			authenticator.require("SHOW_DELETED");
		}
		return repository.findAll(request);
	}

	public int getCountByCategories(int id) {
		return repository.getCount(id);
	}

	@Transactional
	public int publish(ArticlePublishDTO manuscript) {
		authenticator.require("PUBLISH");

		var article = articleMapper.publishToArticle(manuscript);
		article.setUserId(SecurtyContext.getCurrentUser());

		repository.add(article);
		article.setCategories(manuscript.getCategories());

		messageClient.send(new ArticleCreatedEvent(article.getId(), manuscript.getDraftId(), manuscript.getCategories()));
		return article.getId();
	}

	public void update(int id, ArticlePublishDTO publish) {
		Article a = repository.get(id);
		requireModify(a);

		var article = articleMapper.publishToArticle(publish);
		article.setId(id);
		article.setUserId(a.getUserId());
		repository.update(article);

		if (publish.getCategories() != null) {
			article.setCategories(publish.getCategories());
		}

		messageClient.send(new ArticleUpdatedEvent(id, publish.getDraftId(), publish.getCategories()));
	}

	public void changeCategory(int id, List<Integer> categories) {
		requireModify(repository.get(id)).setCategories(categories);
	}

	public void updateDeleteion(int id, boolean isDeleted) {
		var article = repository.get(id);
		if (SecurtyContext.isNotUser(article.getUserId())) {
			authenticator.require("POWER_MODIFY");
		} else {
			authenticator.require("PUBLISH");
		}

		if (isDeleted) {
			article.delete();
		} else {
			article.recover();
		}
	}
}
