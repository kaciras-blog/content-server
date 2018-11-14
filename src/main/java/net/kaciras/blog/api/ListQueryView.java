package net.kaciras.blog.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 为分页查询定义统一的格式。
 *
 * @param <T> 项目类型
 */
@RequiredArgsConstructor
@Getter
public class ListQueryView<T> {

	private final int total;

	@Nullable
	private final List<T> items;

	/**
	 * 创建一个没有项目的视图，一般用于只查询总量而不需要具体内容的请求。
	 *
	 * @param total 总项目数
	 */
	public ListQueryView(int total) {
		this(total, null);
	}
}
