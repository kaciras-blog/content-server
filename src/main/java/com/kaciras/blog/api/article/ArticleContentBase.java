package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Data;
import lombok.ToString;

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
