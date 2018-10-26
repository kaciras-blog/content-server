package net.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.api.category.Banner;
import net.kaciras.blog.api.user.UserVo;

import java.time.LocalDateTime;
import java.util.List;

@ToString(of = {"id", "title"})
@Getter
@Setter
public final class ArticleVo {

	private int id;
	private String urlTitle;
	private UserVo author;
	private int category;
	private boolean deleted;

	private String title;
	private List<String> keywords;
	private String summary;
	private String content;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime create;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime update;

	private int viewCount;
	private int discussionCount;

	private ArticleLink prev;
	private ArticleLink next;
	private Banner banner;
}
