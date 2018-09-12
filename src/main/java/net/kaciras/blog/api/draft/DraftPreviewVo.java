package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DraftPreviewVo extends DraftHistoryVo {

	private int id;
	private Integer articleId;
	private int userId;
}
