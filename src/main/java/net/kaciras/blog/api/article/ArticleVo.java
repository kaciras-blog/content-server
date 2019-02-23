package net.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ToString(of = {"id", "title"})
@Getter
@Setter
public final class ArticleVo {

	private int id;

	// 文章自身属性，注意不包含内容，内容要单独查询
	private String urlTitle;
	private String title;
	private List<String> keywords;
	private String summary;

	// 附加属性
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime create;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime update;

	private int vcnt;
	private int dcnt;
	private ArticleLink prev;
	private ArticleLink next;
	private boolean deleted;

	// 关联其它的领域对象
	private int author;
	private int category;
}
