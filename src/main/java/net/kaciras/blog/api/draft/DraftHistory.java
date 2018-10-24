package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 某次保存的草稿记录。
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class DraftHistory extends DraftContent {

	private int saveCount;
	private int wordCount;

	/** 保存时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;
}
