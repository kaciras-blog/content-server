package net.kaciras.blog.article;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.DeletedState;
import net.kaciras.blog.ListSelectRequest;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ArticleListRequest {

	private Pageable pageable;

	private int userId;

	private Integer category;

	/**
	 * 资源的删除状态过滤，对于无删除状态的资源，此字段将被忽略
	 */
	@NotNull
	private DeletedState deletion = DeletedState.FALSE;
}
