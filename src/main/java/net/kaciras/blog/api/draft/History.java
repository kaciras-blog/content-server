package net.kaciras.blog.api.draft;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 某次保存的草稿记录。
 */
@EqualsAndHashCode(callSuper = false)
@Data
public final class History extends DraftContent {

	private int saveCount;
	private int wordCount;

	/** 保存时间 */
	private LocalDateTime time;
}
