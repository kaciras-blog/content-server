package net.kaciras.blog.domain.draft;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(of = "id", callSuper = false)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
final class Draft extends DraftContentBase {

	/**
	 * 每篇草稿最多保存的历史记录数，默认5
	 */
	@Setter
	private static int historyLimit = 5;

	@Autowired
	private DraftDAO draftDAO;

// - - - - - - - - - - - - - - - - - - - - - -

	@Getter
	@Setter
	private int id;

	@Getter
	@Setter
	private Integer articleId;

	@Getter
	@Setter
	private int userId;

	@Getter
	@Setter
	private LocalDateTime time;

	List<DraftHistory> getHistories() {
		return draftDAO.selectHistories(id);
	}

	/*
	 * 检查数量可能出现幻读（新增）和不可重读（删除），但影响不大
	 */
	void save(DraftContentBase content) {
		int count = draftDAO.selectCountById(id);
		if (count > historyLimit) {
			draftDAO.deleteOldest(id);
		}
		draftDAO.insertHistory(id, content);
	}
}
