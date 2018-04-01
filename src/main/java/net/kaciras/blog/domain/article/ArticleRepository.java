package net.kaciras.blog.domain.article;

import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.kaciras.blog.domain.Utils.checkNotNull;
import static net.kaciras.blog.domain.Utils.checkPositive;

@RequiredArgsConstructor
@Repository
class ArticleRepository {

	private final ArticleDAO articleDAO;
	private final KeywordDAO keywordDAO;

	public Article get(int id) {
		checkPositive(id, "id");
		Article article = articleDAO.selectById(id);
		if (article == null) {
			throw new ResourceNotFoundException();
		}
		return article;
	}

	@Transactional
	public int add(Article article) {
		checkNotNull(article, "article");
		try {
			articleDAO.insert(article);
			article.getKeywords().stream()
					.map(String::trim)
					.filter(k -> !k.isEmpty())
					.forEach(kw -> keywordDAO.insert(article.getId(), kw));
			return article.getId();
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("article中存在不合法的属性值");
		}
	}

	@Transactional
	public void update(Article article) {
		checkNotNull(article, "article");
		try {
			net.kaciras.blog.domain.Utils.checkEffective(articleDAO.update(article));
			keywordDAO.clear(article.getId());
			article.getKeywords().stream()
					.map(String::trim)
					.filter(k -> !k.isEmpty())
					.forEach(kw -> keywordDAO.insert(article.getId(), kw));
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public Observable<Article> getList(ArticleListRequest request) {
		checkNotNull(request, "request");
		request.setCount(Math.min(request.getCount(), 20)); // 限制最大结果数
		return Observable.fromIterable(articleDAO.selectPreview(request));
	}

	@Transactional
	public void delete(int id) {
		if (checkDeleted(id)) {
			throw new IllegalStateException("文章已删除");
		}
		Utils.checkEffective(articleDAO.updateDeleted(id, true));
	}

	@Transactional
	public void recover(int id) {
		if (!checkDeleted(id)) {
			throw new IllegalStateException("文章没有标记为删除");
		}
		Utils.checkEffective(articleDAO.updateDeleted(id, false));
	}

	/**
	 * <code>delete</code>和<code>recover</code>共用的部分抽出来。
	 * 作用是检查id合法性，以及文章是否存在。
	 *
	 * @param id 文章id
	 * @return 删除状态
	 */
	private boolean checkDeleted(int id) {
		checkPositive(id, "id");
		Boolean deleted = articleDAO.selectDeletedById(id);
		if (deleted == null) {
			throw new ResourceNotFoundException("文章不存在");
		}
		return deleted;
	}

}
