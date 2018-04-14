package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.domain.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public final class Article extends ArticleContentBase {

	static ArticleDAO articleDAO;
	static ClassifyDAO classifyDAO;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;

	private LocalDateTime create;
	private LocalDateTime update;
	private int viewCount;
	private int discussionCount;

	private boolean deleted;

	void recordView() {
		articleDAO.increaseViewCount(getId());
	}

	public void delete() {
		if (deleted) {
			throw new IllegalStateException("文章已删除");
		}
		Utils.checkEffective(articleDAO.updateDeleted(id, true));
	}

	public void recover() {
		if (!deleted) {
			throw new IllegalStateException("文章没有标记为删除");
		}
		Utils.checkEffective(articleDAO.updateDeleted(id, false));
	}

	public List<Integer> getCategories() {
		return classifyDAO.selectById(id);
	}

	void setCategories(@NotNull List<Integer> categories) {
		classifyDAO.updateByArticle(id, categories.isEmpty() ? 0 : categories.get(0));
	}
}
