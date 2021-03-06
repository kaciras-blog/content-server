package com.kaciras.blog.api.draft;

import com.kaciras.blog.api.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Configurable
public final class HistoryList {

	/** 每篇草稿最多保存的历史记录数 */
	private static final int historyLimit = 10;

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

	public History findBySaveCount(int saveCount) {
		return historyDAO.select(id, saveCount);
	}

	// 不包含 content
	public History findLatest() {
		var latest = historyDAO.selectLastSaveCount(id);
		if (latest == null) {
			throw new Error("草稿主表与历史表不一致，id=" + id);
		}
		return historyDAO.select(id, latest);
	}

	public List<History> findAll() {
		return historyDAO.selectAll(id);
	}

	public void update(DraftContent content) {
		var last = historyDAO.selectLastSaveCount(id);
		Utils.checkEffective(historyDAO.update(id, last, content));
	}
}
