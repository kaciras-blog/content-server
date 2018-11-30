package other.pkg;

import net.kaciras.blog.api.article.ArticleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class SubModuleTests {

	@Test
	void testComponentsScan() {
		var app = new SpringApplicationBuilder(TestApplication.class)
				.profiles("dev").run();

		Assertions.assertThat(app.getBean(AnTestBean.class)).isNotNull();
		Assertions.assertThatThrownBy(() -> app.getBean(ArticleRepository.class))
				.isInstanceOf(NoSuchBeanDefinitionException.class);

		app.close();
	}

	@Test
	void test() {
		var app = new SpringApplicationBuilder(TestApplicationWithScan.class)
				.profiles("dev").run();

		Assertions.assertThat(app.getBean(AnTestBean.class)).isNotNull();
		Assertions.assertThat(app.getBean(ArticleRepository.class)).isNotNull();

		app.close();
	}
}
