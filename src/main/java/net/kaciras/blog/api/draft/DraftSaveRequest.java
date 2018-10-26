package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class DraftSaveRequest extends DraftContent {

	private int id;
	private Integer articleId;
}
