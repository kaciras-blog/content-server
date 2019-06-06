package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageReference;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DraftContent {

	@NotEmpty
	private String title;

	@NotNull
	private ImageReference cover;

	@NotNull
	private String keywords;

	@NotNull
	private String summary;

	@NotNull
	private String content;

	/**
	 * 创建一个新的草稿内容，所有字段设置为默认值。
	 *
	 * @return 草稿内容对象
	 */
	public static DraftContent initial() {
		var content = new DraftContent();
		content.setTitle("新文章");
		content.setSummary("");
		content.setKeywords("");
		content.setContent("");
		content.setCover(ImageReference.parse("placeholder.png"));
		return content;
	}
}
