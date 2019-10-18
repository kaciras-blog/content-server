package spring;

import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.infra.Misc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public final class SubModuleTests {

	/**
	 * 跟正常启动一样，先关闭两个无聊的东西。
	 *
	 * @throws Exception 如果发生错误
	 */
	@BeforeAll
	static void setUpClass() throws Exception {
		Misc.disableIllegalAccessWarning();
		Misc.disableHttpClientCertificateVerify();
	}

	/** 验证默认的组件扫描仅扫描被注解类所在的包及其下级包 */
	@Test
	void testAutoComponentsScan() {
		var builder = new SpringApplicationBuilder(TestApplication.class).profiles("dev");

		try (var app = builder.run()) {
			Assertions.assertThat(app.getBean(AnTestBean.class)).isNotNull();

			Assertions.assertThatThrownBy(() -> app.getBean(ArticleRepository.class))
					.isInstanceOf(NoSuchBeanDefinitionException.class);
			Assertions.assertThatThrownBy(() -> app.getBean("principalAspect"))
					.isInstanceOf(NoSuchBeanDefinitionException.class);
		}
	}
}
