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

	public static DraftContent initial() {
		var content = new DraftContent();
		content.setTitle("");
		content.setSummary("");
		content.setKeywords("");
		content.setContent("");
		content.setCover(ImageRefrence.parse("placeholder.png"));
		return content;
	}
}
