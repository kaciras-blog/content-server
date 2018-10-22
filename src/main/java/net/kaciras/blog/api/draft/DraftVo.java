package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public final class DraftVo {

	private int id;
	private Integer articleId;
	private int userId;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;


}
