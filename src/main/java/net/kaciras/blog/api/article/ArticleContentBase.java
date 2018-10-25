package net.kaciras.blog.api.article;

import lombok.Data;
import lombok.ToString;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.util.List;

@ToString(of = "title")
@Data
abstract class ArticleContentBase {

	private String title;

	private List<String> keywords;

	private ImageRefrence cover;

	private String summary;

	private String content;
}
