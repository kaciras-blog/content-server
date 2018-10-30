package net.kaciras.blog.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ListQueryView<T> {

	private final int total;

	@Nullable
	private final List<T> items;

	public ListQueryView(int total) {
		this(total, null);
	}
}
