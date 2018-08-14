package net.kaciras.blog.pojo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public final class ArticleVo {

	private int id;
	private UserVo author;

	private String title;
	private List<String> keywords;
	private String summary;
	private String content;

	private List<Integer> categories;

	private LocalDateTime create;
	private LocalDateTime update;
}
