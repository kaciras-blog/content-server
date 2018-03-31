package net.kaciras.blog.facade.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DraftHistoryVO {

	private String title;
	private String cover;
	private String summary;

	private int saveCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;
}
