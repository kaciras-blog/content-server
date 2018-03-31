package net.kaciras.blog.facade.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DraftPreviewVO extends DraftHistoryVO {

	private int id;
	private Integer articleId;
	private int userId;
}
