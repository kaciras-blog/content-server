package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public final class Article extends ArticleContentBase {

	static ArticleDAO articleDAO;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;

	private LocalDateTime create;
	private LocalDateTime update;
	private int viewCount;
	private int discussionCount;

	private boolean deleted;

	void recordView() {
		articleDAO.increaseViewCount(getId());
	}
}
