package net.kaciras.blog.domain;

import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.api.SecurtyContext;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

class ArticleServiceTest extends AbstractSpringTest {

	@Autowired
	private ArticleService articleService;

	@BeforeAll
	static void setupClass() {
		SecurtyContext.setCurrentUser(1);
	}

}
