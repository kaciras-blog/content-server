package net.kaciras.blog.api.draft;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class HistoryRepository {

	/**
	 * 每篇草稿最多保存的历史记录数，默认5
	 */
	private static final int historyLimit = 5;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DraftDAO draftDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private HistoryDAO historyDAO;


// - - - - - - - - - - - - - - - - - - - - - -

	private final int id;

	/**
	 * 保存草稿的内容为一个新的历史记录。
	 * 检查数量可能出现幻读（新增）和不可重读（删除），但影响不大
	 *
	 * @param content 内容
	 */
	public int add(DraftContent content) {
		var count = historyDAO.selectCount(id);
		if (count >= historyLimit) {
			historyDAO.deleteOldest(id);
		}

		var newSaveCount = historyDAO.selectLastSaveCount(id);
		newSaveCount = newSaveCount == null ? 0 : newSaveCount + 1;
		historyDAO.insert(id, newSaveCount, content);

		return newSaveCount;
	}

	public List<DraftHistory> getAll() {
		return historyDAO.selectAll(id);
	}

	public void update(DraftContent content) {
		var last = historyDAO.selectLastSaveCount(id);
		DBUtils.checkEffective(historyDAO.update(id, last, content));
	}
}
