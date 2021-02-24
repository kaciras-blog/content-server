package com.kaciras.blog.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 为分页查询定义统一的格式，包含一个总数和元素的列表。
 *
 * @param <T> 项目类型
 */
@RequiredArgsConstructor
@Getter
public class ListQueryView<T> {

	private final int total;

	@NonNull
	private final List<T> items;
}
