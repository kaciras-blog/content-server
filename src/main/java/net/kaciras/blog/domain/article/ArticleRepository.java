package net.kaciras.blog.domain.article;

import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.kaciras.blog.domain.Utils.checkNotNull;
import static net.kaciras.blog.domain.Utils.checkPositive;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
class ArticleRepository {

	private final ArticleDAO articleDAO;
	private final KeywordDAO keywordDAO;
	private final ClassifyDAO classifyDAO;

	@NotNull
	public Article get(int id) {
		checkPositive(id, "id");
		Article article = articleDAO.selectById(id);
		if (article == null) {
			throw new ResourceNotFoundException();
		}
		return article;
	}

	@Transactional
	public void add(Article article) {
		checkNotNull(article, "article");
		try {
			articleDAO.insert(article);
			article.getKeywords().stream()
					.map(String::trim)
					.filter(k -> !k.isEmpty())
					.forEach(kw -> keywordDAO.insert(article.getId(), kw));
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("article中存在不合法的属性值");
		}
	}

	@Transactional
	public void update(Article article) {
		checkNotNull(article, "article");
		try {
			Utils.checkEffective(articleDAO.update(article));
			keywordDAO.clear(article.getId());
			article.getKeywords().stream()
					.map(String::trim)
					.filter(k -> !k.isEmpty())
					.forEach(kw -> keywordDAO.insert(article.getId(), kw));
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	Observable<Article> findAll(ArticleListRequest request) {
		checkNotNull(request, "request");
		request.setCount(Math.min(request.getCount(), 20)); // 限制最大结果数
		return Observable.fromIterable(articleDAO.selectPreview(request));
	}

}
