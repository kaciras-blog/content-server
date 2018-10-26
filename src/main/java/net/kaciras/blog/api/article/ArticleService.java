package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.event.category.CategoryRemovedEvent;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleService {

	private final ArticleRepository repository;
	private final ClassifyDAO classifyDAO;

	private final ArticleMapper mapper;
	private final MessageClient messageClient;

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`,.!]+");

	@PostConstruct
	private void init() {
		messageClient.subscribe(CategoryRemovedEvent.class, event ->
				classifyDAO.updateCategory(event.getId(), event.getParent()));
	}

	/**
	 * 检查用户是否有权限更改指定的的文章。
	 *
	 * @param article 文章
	 * @throws PermissionException 如果没权限
	 */
	private void requireModify(Article article) {
		if (article.isDeleted()) {
			throw new ResourceDeletedException();
		}
		SecurityContext.requireSelf(article.getUserId(), "POWER_MODIFY");
	}

	public Article getArticle(int id) {
		var article = repository.get(id);

		if (article.isDeleted()
				&& SecurityContext.checkSelf(article.getUserId(), "SHOW_DELETED")) {
			throw new ResourceDeletedException();
		}
		return article;
	}

	public List<PreviewVo> getList(ArticleListQuery request) {
		if (request.getDeletion() != DeletedState.FALSE) {
			SecurityContext.require("SHOW_DELETED");
		}
		return repository.findAll(request)
				.stream()
				.map(article -> mapper.toPreview(article, request))
				.collect(Collectors.toList());
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
	public int publish(PublishRequest request) {
		SecurityContext.require("PUBLISH");

		var article = mapper.toArticle(request);
		article.setUserId(SecurityContext.getUserId());

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
	public void update(int id, PublishRequest update) {
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
		repository.get(id).updateCategory(category);
	}

	public void updateDeleteion(int id, boolean isDeleted) {
		var article = repository.get(id);

		if (SecurityContext.isNot(article.getUserId())) {
			SecurityContext.require("POWER_MODIFY");
		} else {
			SecurityContext.require("PUBLISH");
		}
		article.updateDeleted(isDeleted);
	}
}
