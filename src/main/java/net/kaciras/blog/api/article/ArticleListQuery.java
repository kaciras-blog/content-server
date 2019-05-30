package net.kaciras.blog.api.article;

import lombok.*;
import net.kaciras.blog.api.DeletedState;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public final class ArticleListQuery {

	private Pageable pageable;

	private int userId;

	private int category;
	private boolean recursive;

	/**
	 * 资源的删除状态过滤，对于无删除状态的资源，此字段将被忽略
	 */
	@NotNull
	private DeletedState deletion = DeletedState.FALSE;

	public static ArticleListQuery ofCategory(int id, boolean recursive) {
		return new ArticleListQuery(null, 0, id, recursive, DeletedState.FALSE);
	}
}
