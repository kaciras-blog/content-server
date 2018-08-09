package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.util.List;

@Getter
@Setter
abstract class ArticleContentBase {

	private int userId;

	private String title;

	private List<String> keywords;

	private ImageRefrence cover;

	private String summary;

	private String content;
}
