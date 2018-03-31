package net.kaciras.blog.domain;

import net.kaciras.blog.domain.article.ArticleService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class ArticleTest extends AbstractSpringTest {

	@Autowired
	private ArticleService articleService;

	@Test
	void testCountByCategories() {
		int count = articleService.getCountByCategories(Arrays.asList(1, 2, 3));
		Assertions.assertThat(count).isEqualTo(2);

		Assertions.assertThat(articleService.getCountByCategories0(1)).isEqualTo(2);
	}
}
