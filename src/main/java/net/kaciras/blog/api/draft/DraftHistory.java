package net.kaciras.blog.api.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Data
public final class DraftHistory {

	private int saveCount;
	private int wordCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime time;

	@Nullable
	private DraftContent content;
}
