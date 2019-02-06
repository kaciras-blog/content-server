package net.kaciras.blog.api.article;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class ArticleRepository {

	private final ArticleDAO articleDAO;
	private final KeywordDAO keywordDAO;
	private final ClassifyDAO classifyDAO;

	/**
	 * 查询一篇文章，如果文章不存在则抛出 ResourceNotFoundException 异常。
	 *
	 * @param id 文章ID
	 * @return 文章对象
	 * @throws ResourceNotFoundException 如果指定ID的文章不存在
	 */
	@NonNull
	public Article get(int id) {
		return articleDAO.selectById(id).orElseThrow(ResourceNotFoundException::new);
	}

	@Transactional
	public void add(@NonNull Article article) {
		try {
			articleDAO.insert(article);
			insertKeywords(article.getId(), article.getKeywords());
		} catch (DataIntegrityViolationException ex) {
			throw new RequestArgumentException();
		}
	}

	@Transactional
	public void update(@NonNull Article article) {
		try {
			DBUtils.checkEffective(articleDAO.update(article));
			keywordDAO.clear(article.getId());
			insertKeywords(article.getId(), article.getKeywords());
		} catch (DataIntegrityViolationException ex) {
			throw new RequestArgumentException(ex);
		}
	}

	private void insertKeywords(int articleId, List<String> keywords) {
		keywords.stream().map(String::trim)
				.filter(kw -> !kw.isEmpty())
				.forEach(kw -> keywordDAO.insert(articleId, kw));
	}

	public List<Article> findAll(@NonNull ArticleListQuery request) {
		return articleDAO.selectPreview(request);
	}

	public int size() {
		return articleDAO.selectCount();
	}

	public int countByCategory(int id) {
		return classifyDAO.selectCountByCategory(id);
	}
}
