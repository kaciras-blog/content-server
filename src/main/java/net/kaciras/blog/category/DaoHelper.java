package net.kaciras.blog.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class DaoHelper {

	private final CategoryDAO dao;

	void requireContains(int id) {
		Boolean v = dao.contains(id);
		if (v == null || !v) throw new IllegalArgumentException("指定的分类不存在");
	}

}
