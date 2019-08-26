package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.api.category.Banner;

import java.time.LocalDateTime;
import java.util.List;

@ToString(of = {"id", "title"})
@Getter
@Setter
public final class ArticleVo {

	private int id;
	private String urlTitle;
	private int category;
	private boolean deleted;

	private String title;
	private List<String> keywords;
	private String summary;
	private String content;

	private LocalDateTime create;
	private LocalDateTime update;

	private int viewCount;
	private int discussionCount;

	private Banner banner;
	private ArticleLink prev;
	private ArticleLink next;
}
