package net.kaciras.blog.domain.article;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.message.MessageClient;
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

//	@Autowired
//	public void setPermissionStore(PermissionRepository store) {
//		store.regist(Permission.of("ArticleService", "PUBLISH", "发表文章"), true);
//		store.regist(Permission.of("ArticleService", "SHOW_DELETED", "查看被删除的文章"), true);
//		store.regist(Permission.of("ArticleService", "MODIFY_OTHER", "修改或删除他人的文章"), true);
//	}

	private boolean isDisallow(String perm) {
		return !SecurtyContext.accept("ArticleService", perm);
	}

	/**
	 * 检查用户是否有权限更改不属于自己的文章。
	 *
	 * @param articleId 文章id
	 * @throws SecurityException 如果没权限，且修改的文章不属于自己
	 */
	private void checkModifyOther(int articleId) {
		if (isDisallow("MODIFY_OTHER")) {
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
		if (article.isDeleted()) {
			if (isDisallow("SHOW_DELETED"))
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
		hots = articleRepository.getList(request).map(articleMapper::toDTO);
	}

	public Observable<ArticleDTO> getList(ArticleListRequest request) {
		if (isDisallow("SHOW_DELETED") && request.isShowDeleted()) {
			throw new PermissionException();
		}
		return articleRepository.getList(request).map(articleMapper::toDTO);
	}

	public int getCountByCategories(List<Integer> ids) {
		return classifyDAO.selectCountByCategory2(ids);
	}

	@Deprecated
	public int getCountByCategories0(int id) {
		return classifyDAO.selectCountByCategory(id);
	}

	@Transactional
	public int publish(ArticlePublishDTO publish) {
		if (isDisallow("PUBLISH")) {
			throw new PermissionException();
		}
		publish.setUserId(SecurtyContext.getCurrentUser());
		int id = articleRepository.add(articleMapper.publishToArticle(publish));
		classifyDAO.updateByArticle(id, publish.getCategories().get(0));

		messageClient.send(new ArticleCreatedEvent(id, publish.getDraftId(), publish.getCategories()));
		return id;
	}

	@Transactional
	public void update(int id, ArticlePublishDTO publish) {
		checkModifyOther(id);
		Article article = articleMapper.publishToArticle(publish);
		article.setId(id);
		articleRepository.update(article);
		int category = 0;
		if (!publish.getCategories().isEmpty()) {
			category = publish.getCategories().get(0); //TODO: #BUG NullPointer
		}
		classifyDAO.updateByArticle(id, category);

		messageClient.send(new ArticleUpdatedEvent(id, publish.getDraftId(), publish.getCategories()));
	}

	public void delete(int id) {
		checkModifyOther(id);
		articleRepository.delete(id);
	}
}
