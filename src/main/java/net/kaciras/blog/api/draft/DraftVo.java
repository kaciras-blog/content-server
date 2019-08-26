package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public final class DraftVo {

	private int id;
	private Integer articleId;
	private int userId;

	private String title;
	private int lastSaveCount;

	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
