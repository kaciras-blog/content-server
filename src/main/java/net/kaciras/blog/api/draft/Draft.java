package net.kaciras.blog.api.draft;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

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
