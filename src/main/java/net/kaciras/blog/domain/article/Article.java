package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static net.kaciras.blog.domain.Utils.checkPositive;

@Getter
@Setter
public final class Article extends ArticleContentBase {

	static ArticleDAO articleDAO;

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
		if (checkDeleted(id)) {
			throw new IllegalStateException("文章已删除");
		}
		Utils.checkEffective(articleDAO.updateDeleted(id, true));
	}

	public void recover() {
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
