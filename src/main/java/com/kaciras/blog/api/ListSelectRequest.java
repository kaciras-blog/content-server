package com.kaciras.blog.api;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页查询请求对象，包括了各种在列表查询中将使用的数据。
 * 这些字段并不都被使用，具体取决于服务方。
 */
@Data
public class ListSelectRequest implements Serializable {

	private Pageable pageable;

	/** 资源的删除状态过滤，对于无删除状态的资源，此字段将被忽略 */
	@NotNull
	private DeletedState deletion = DeletedState.ALIVE;
}
