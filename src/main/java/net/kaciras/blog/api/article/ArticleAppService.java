package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleAppService {

	private final ArticleRepository repository;
	private final MessageClient messageClient;

	public List<Article> getList(ArticleListQuery query) {
		if (query.getDeletion() != DeletedState.FALSE) {
			SecurityContext.require("SHOW_DELETED");
		}
		return repository.findAll(query);
	}

	/**
	 * 获取一个文章，同时检查文章的删除状态以及用户是否具有查看删除文章的权限。
	 *
	 * @param id      文章ID
	 * @param outside 是否由外部访问的
	 * @return 文章对象
	 * @throws ResourceDeletedException 如果文章被标记为删除，且用户没有查看权限
	 */
	public Article getArticle(int id, boolean outside) {
		var article = repository.get(id).orElseThrow(ResourceNotFoundException::new);

		if (article.isDeleted()
				&& SecurityContext.checkSelf(article.getUserId(), "SHOW_DELETED")) {
			throw new ResourceDeletedException();
		}
		if (outside) {
			article.recordView(); // 增加浏览量
		}
		return article;
	}

	@RequireAuthorize
	public void addNew(Article article, int draftId) {
		try {
			repository.add(article);
			messageClient.send(new ArticleCreatedEvent(article.getId(), draftId, article.getCategory()));
		} catch (DataIntegrityViolationException ex) {
			throw new RequestArgumentException();
		}
	}

	@RequireAuthorize
	@Transactional
	public void update(int id, PatchMap patchMap) {
		var article = repository.get(id).orElseThrow(ResourceNotFoundException::new);

		if (patchMap.getCategory() != null) {
			article.updateCategory(patchMap.getCategory());
		}
		if (patchMap.getDeletion() != null) {
			article.updateDeleted(patchMap.getDeletion());
		}
		if(patchMap.getUrlTitle() != null) {
			article.updateUrlTitle(patchMap.getUrlTitle());
		}
	}
}
