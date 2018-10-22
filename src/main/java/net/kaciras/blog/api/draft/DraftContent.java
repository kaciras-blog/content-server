package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Getter
@Setter
public class DraftContent {

	private String title;
	private ImageRefrence cover;
	private String keywords;
	private String summary;
	private String content;
}
