package net.kaciras.blog.article;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public final class ArticlePublishDTO extends ArticleContentBase implements Serializable {

	private List<Integer> categories;
	private int draftId;
}
