package net.kaciras.blog.draft;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Getter
@Setter
public abstract class DraftContentBase {

	private String title;
	private ImageRefrence cover;
	private String summary;
	private String keywords;
	private String content;
}
