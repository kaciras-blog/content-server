package net.kaciras.blog.domain.article;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public final class ArticlePublishDTO extends ArticleContentBase implements Serializable {

	private int draftId;
}
