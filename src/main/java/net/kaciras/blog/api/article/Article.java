package net.kaciras.blog.api.article;

import lombok.*;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

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
	private int userId;

	private LocalDateTime create;
	private LocalDateTime update;
	private boolean deleted;

	private int viewCount;

	void recordView() {
		articleDAO.increaseViewCount(getId());
	}

	/**
	 * 更改文章的删除状态，如果操作没有意义（当前状态与目标状态相同）则抛出异常。
	 *
	 * @param value 目标状态，true表示删除，false表示没有删除。
	 */
	public void updateDeleted(boolean value) {
		if (deleted == value) {
			if(deleted) {
				throw new ResourceDeletedException("文章已经删除了");
			}
			throw new ResourceStateException("文章还没有被删除呢");
		}
		articleDAO.updateDeleted(id, value);
	}

	public int getCategory() {
		return classifyDAO.selectById(id);
	}

	public void updateCategory(int category) {
		classifyDAO.updateByArticle(id, category);
	}
}
