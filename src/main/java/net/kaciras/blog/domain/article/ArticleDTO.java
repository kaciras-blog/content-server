package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class ArticleDTO extends ArticleContentBase implements Serializable {

	private int id;
	private LocalDateTime create;
	private LocalDateTime update;
	private int viewCount;
	private int discussionCount;
}
