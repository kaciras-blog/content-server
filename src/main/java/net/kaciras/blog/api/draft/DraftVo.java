package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime updateTime;
}
