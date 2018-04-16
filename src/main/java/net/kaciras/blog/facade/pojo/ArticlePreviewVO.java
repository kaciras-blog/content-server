package net.kaciras.blog.facade.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public final class ArticlePreviewVO {

	private int id;
	private UserVO author;
	private List<CategoryVO> categoryPath;

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
