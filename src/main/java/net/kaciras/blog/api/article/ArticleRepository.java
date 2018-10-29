package net.kaciras.blog.api.article;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.kaciras.blog.api.Utils.checkNotNull;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class ArticleRepository {

	private final ArticleDAO articleDAO;
	private final KeywordDAO keywordDAO;
	private final ClassifyDAO classifyDAO;

	public Article get(int id) {
		return DBUtils.checkNotNullResource(articleDAO.selectById(id));
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
			throw new RequestArgumentException();
		}
	}

	@Transactional
	public void update(Article article) {
		checkNotNull(article, "article");
		try {
			DBUtils.checkEffective(articleDAO.update(article));
			keywordDAO.clear(article.getId());
			article.getKeywords().stream()
					.map(String::trim)
					.filter(k -> !k.isEmpty())
					.forEach(kw -> keywordDAO.insert(article.getId(), kw));
		} catch (DataIntegrityViolationException ex) {
			throw new RequestArgumentException(ex);
		}
	}

	public List<Article> findAll(ArticleListQuery request) {
		checkNotNull(request, "request");
		return articleDAO.selectPreview(request);
	}

	public int getCount(int id) {
		return classifyDAO.selectCountByCategory(id);
	}
}
