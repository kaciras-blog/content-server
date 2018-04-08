package net.kaciras.blog.domain.article;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
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

	private final ArticleRepository articleRepository;
	private final ClassifyDAO classifyDAO;
	private final ArticleMapper articleMapper;

	private final MessageClient messageClient;

	private Observable<ArticleDTO> hots;

	private Authenticator authenticator;

	@Autowired
	public void setAuthenticator(AuthenticatorFactory factory) {
		this.authenticator = factory.create("ARTICLE");
	}

	/**
	 * 检查用户是否有权限更改不属于自己的文章。
	 *
	 * @param articleId 文章id
	 * @throws SecurityException 如果没权限，且修改的文章不属于自己
	 */
	private void checkModifyOther(int articleId) {
		if (!authenticator.check("POWER_MODIFY")) {
			Integer userId = SecurtyContext.getCurrentUser();
			Article old = articleRepository.get(articleId);
			if (userId == null || old.getUserId() != userId) {
				throw new PermissionException();
			}
		}
	}

	public Observable<ArticleDTO> getHots() {
		return hots;
	}

	public Single<ArticleDTO> getArticle(int id) {
		Article article = articleRepository.get(id);
		if (article.isDeleted() && !authenticator.check("SHOW_DELETED")) {
			throw new ResourceDeletedException();
		}
		return Single.just(article)
				.doAfterSuccess(Article::recordView) //增加浏览量
				.map(articleMapper::toDTO);
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	void updateHotsTask() {
		ArticleListRequest request = new ArticleListRequest();
		request.setDesc(true);
		request.setSort("view_count");
		request.setCount(6);
		hots = articleRepository.findAll(request).map(articleMapper::toDTO);
	}

	public Observable<ArticleDTO> getList(ArticleListRequest request) {
		if (request.isShowDeleted()) {
			authenticator.require("SHOW_DELETED");
		}
		return articleRepository.findAll(request).map(articleMapper::toDTO);
	}

	public int getCountByCategories(List<Integer> ids) {
		return classifyDAO.selectCountByCategory2(ids);
	}

	@Deprecated
	public int getCountByCategories0(int id) {
		return classifyDAO.selectCountByCategory(id);
	}

	public int publish(ArticlePublishDTO publish) {
		authenticator.require("PUBLISH");

		Article article = articleMapper.publishToArticle(publish);
		article.setUserId(SecurtyContext.getCurrentUser());
		int id = articleRepository.add(article);

		messageClient.send(new ArticleCreatedEvent(id, publish.getDraftId(), publish.getCategories()));
		return id;
	}

	public void update(int id, ArticlePublishDTO publish) {
		if(SecurtyContext.isNotUser(id)) {
			throw new PermissionException();
		}
		Article article = articleMapper.publishToArticle(publish);
		article.setId(id);
		articleRepository.update(article);
		messageClient.send(new ArticleUpdatedEvent(id, publish.getDraftId(), publish.getCategories()));
	}

	public void delete(int id) {
		checkModifyOther(id);
		articleRepository.get(id).delete();
	}
}
