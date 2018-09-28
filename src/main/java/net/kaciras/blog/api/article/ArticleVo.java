package net.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.user.UserVo;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime create;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime update;

	private int viewCount;
	private int discussionCount;

	private ArticleLink prev;
	private ArticleLink next;
	private ImageRefrence banner;
}
