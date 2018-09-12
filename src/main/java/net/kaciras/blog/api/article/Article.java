package net.kaciras.blog.api.article;

import lombok.*;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Configurable
public class Article extends ArticleContentBase {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ArticleDAO articleDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ClassifyDAO classifyDAO;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;

	private LocalDateTime create;
	private LocalDateTime update;
	private int viewCount;
//	private int discussionCount;

	private boolean deleted;

	void recordView() {
		articleDAO.increaseViewCount(getId());
	}

	public void delete() {
		if (deleted) {
			throw new IllegalStateException("文章已删除");
		}
		DBUtils.checkEffective(articleDAO.updateDeleted(id, true));
	}

	public void recover() {
		if (!deleted) {
			throw new IllegalStateException("文章没有标记为删除");
		}
		DBUtils.checkEffective(articleDAO.updateDeleted(id, false));
	}

	public List<Integer> getCategories() {
		return classifyDAO.selectById(id);
	}

	public void setCategories(@NonNull List<Integer> categories) {
		classifyDAO.updateByArticle(id, categories.isEmpty() ? 0 : categories.get(0));
	}
}
