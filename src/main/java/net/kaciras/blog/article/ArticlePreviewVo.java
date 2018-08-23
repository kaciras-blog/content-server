package net.kaciras.blog.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.category.CategoryVo;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.user.UserVo;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public final class ArticlePreviewVo {

	private int id;
	private UserVo author;
	private List<CategoryVo> categoryPath;

	private String title;
	private List<String> keywords;
	private ImageRefrence cover;
	private String summary;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime create;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime update;

	private int viewCount;
	private int discussionCount;

	private boolean deleted;
}
