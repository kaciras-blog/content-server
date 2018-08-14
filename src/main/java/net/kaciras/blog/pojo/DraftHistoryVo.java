package net.kaciras.blog.pojo;

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
	private LocalDateTime time;
}
