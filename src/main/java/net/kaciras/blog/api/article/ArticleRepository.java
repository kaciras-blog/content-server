package net.kaciras.blog.api.article;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
	public Optional<Article> get(int id) {
		return articleDAO.selectById(id);
	}

	/**
	 * 添加一篇文章，在添加后新生成的ID将被设置。
	 *
	 * @param article 文章对象
	 */
	@Transactional
	public void add(@NonNull Article article) {
		articleDAO.insert(article);
		insertKeywords(article.getId(), article.getKeywords());
	}

	@Transactional
	public void update(@NonNull Article article) {
		DBUtils.checkEffective(articleDAO.update(article));
		keywordDAO.clear(article.getId());
		insertKeywords(article.getId(), article.getKeywords());
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
