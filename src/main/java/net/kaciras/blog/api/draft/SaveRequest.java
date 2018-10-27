package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
final class SaveRequest extends DraftContent {

	private int id;
	private Integer articleId;
}
