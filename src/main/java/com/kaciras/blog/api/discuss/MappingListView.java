package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.ListQueryView;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class MappingListView<K, T> extends ListQueryView<K> {

	private final Map<K, T> objects;

	public MappingListView(int total, List<K> items, Map<K, T> objects) {
		super(total, items);
		this.objects = objects;
	}
}
