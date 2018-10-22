package net.kaciras.blog.api.draft;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = "id", callSuper = false)
@Data
@Configurable
public final class Draft {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DraftDAO draftDAO;

// - - - - - - - - - - - - - - - - - - - - - -

	private int id;

	private Integer articleId;

	private int userId;

	private LocalDateTime time;

	public HistoryRepository getHistoryList() {
		return new HistoryRepository(id);
	}
}
