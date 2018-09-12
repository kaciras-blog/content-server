package net.kaciras.blog.api;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页查询请求对象，包括了各种在列表查询中将使用的数据。
 * 这些字段并不都被使用，具体取决于服务方。
 * <p>
 * PS: 使用时注意防范SQL注入
 */
@Data
public class ListSelectRequest implements Serializable {

	/**
	 * 起始信息，可能是页码、最后一个结果的id或是其他信息，具体取决于服务
	 */
	private int start;

	/**
	 * 结果数，服务可能对结果数范围有限制
	 */
	private int count;

	/**
	 * 排序字段和是否倒序。
	 */
	private String sort;
	private boolean desc;

	/**
	 * 资源的删除状态过滤，对于无删除状态的资源，此字段将被忽略
	 */
	@NotNull
	private DeletedState deletion = DeletedState.FALSE;
}
