package com.kaciras.blog.api.article;

import com.kaciras.blog.api.DeletedState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public final class ArticleListQuery {

	private int userId;

	private int category;
	private boolean recursive;

	/** 是否包含文章内容 */
	// TODO: 搞复杂了，下一版必须要重新设计API
	private boolean content;

	private Pageable pageable;

	@NotNull
	private DeletedState deletion = DeletedState.ALIVE;
}
