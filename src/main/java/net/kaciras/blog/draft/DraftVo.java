package net.kaciras.blog.draft;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class DraftVo extends DraftPreviewVo {

	private String keywords;
	private String content;
}
