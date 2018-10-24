package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public final class DraftVo {

	private int id;
	private Integer articleId;
	private int userId;

	private String title;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime updateTime;
}
