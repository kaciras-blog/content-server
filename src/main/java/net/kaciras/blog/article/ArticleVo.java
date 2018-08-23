package net.kaciras.blog.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.user.UserVo;

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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime create;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime update;
}
