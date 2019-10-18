package net.kaciras.blog.api.article;

import lombok.Data;
import lombok.ToString;
import net.kaciras.blog.infra.codec.ImageReference;

import java.util.List;

@ToString(of = "title")
@Data
public abstract class ArticleContentBase {

	private String title;

	private List<String> keywords;

	private ImageReference cover;

	private String summary;

	private String content;
}
