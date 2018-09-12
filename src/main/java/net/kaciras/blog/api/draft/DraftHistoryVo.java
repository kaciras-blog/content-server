package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.time.LocalDateTime;

@Getter
@Setter
public class DraftHistoryVo {

	private String title;
	private ImageRefrence cover;
	private String summary;

	private int saveCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;
}
