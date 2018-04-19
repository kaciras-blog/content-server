package net.kaciras.blog.domain.draft;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.kaciras.blog.domain.Utils;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(of = "id", callSuper = false)
@Data
final class Draft extends DraftContentBase {

	/**
	 * 每篇草稿最多保存的历史记录数，默认5
	 */
	@Setter
	private static int historyLimit = 5;

	static DraftDAO draftDAO;

// - - - - - - - - - - - - - - - - - - - - - -

	private int id;
	private Integer articleId;
	private int userId;
	private int saveCount;
	private LocalDateTime time;

	List<DraftHistory> getHistories() {
		return draftDAO.selectHistories(id);
	}

	/**
	 * 保存草稿。
	 *
	 * @param content 内容
	 */
	void save(DraftContentBase content) {
		Utils.checkEffective(draftDAO.update(this, content));
	}

	/**
	 * 保存草稿的内容为一个新的历史记录。
	 * 检查数量可能出现幻读（新增）和不可重读（删除），但影响不大
	 *
	 * @param content 内容
	 */
	void saveNewHistory(DraftContentBase content) {
		int count = draftDAO.selectCountById(id);
		if (count > historyLimit) {
			draftDAO.deleteOldest(id);
		}
		draftDAO.insertHistory(id, content);
	}
}
