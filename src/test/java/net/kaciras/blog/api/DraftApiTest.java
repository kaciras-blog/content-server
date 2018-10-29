package net.kaciras.blog.api;

import net.kaciras.blog.api.category.Category;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DraftApiTest extends AbstractSpringTest {

	@Test
	void testGetList() throws Exception{
		var c = new Category();
		var dao = Category.class.getDeclaredField("dao");
		dao.setAccessible(true);
		var res = dao.get(c);
		Assertions.assertThat(res).isNotNull();
	}
}
