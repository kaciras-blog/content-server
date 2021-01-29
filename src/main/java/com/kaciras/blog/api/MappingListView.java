package com.kaciras.blog.api;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 与 ListQueryView 一样表示分页查询结果，但其中列表项为对象的ID，
 * 而真正的对象在 objects 这个 Map 里。
 * <p>
 * 该类用于查询结果存在重复项时去除重复的信息，以减小响应体积。
 *
 * @param <K> 项目 ID 的类型
 * @param <T> 项目的类型
 */
@Getter
public class MappingListView<K, T> extends ListQueryView<K> {

	private final Map<K, T> objects;

	public MappingListView(int total, List<K> items, Map<K, T> objects) {
		super(total, items);
		this.objects = objects;
	}
}
