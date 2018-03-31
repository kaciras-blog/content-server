package net.kaciras.blog.domain.draft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DraftContentBase {

	private String title;
	private String cover;
	private String summary;
	private String keywords;
	private String content;
}
