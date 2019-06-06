package net.kaciras.blog.api.draft;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

/**
 * 草稿对象，该对象本身不包含草稿的内容，只有一些元数据。
 * 草稿对象包含了历史记录的列表，每个历史记录包含草稿的内容。
 */
@EqualsAndHashCode(of = "id")
@Data
@Configurable
public final class Draft {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DraftDAO draftDAO;

// - - - - - - - - - - - - - - - - - - - - - -

	private int id;
	private int userId;

	/** 文章来源，null表示新文章，否则表示修改文章 */
	private Integer articleId;

	/** 创建时间 */
	private LocalDateTime time;

	public HistoryList getHistoryList() {
		return new HistoryList(id);
	}
}
