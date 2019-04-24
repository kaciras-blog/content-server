package spring;

import net.kaciras.blog.api.ServiceApplication;
import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.infrastructure.Misc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

@ExtendWith(SpringExtension.class)
public class SubModuleTests {

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

	/**
	 * 验证默认的组件扫描仅扫描被注解类所在的包及其下级包。
	 */
	@Test
	void testAutoComponentsScan() {
		var builder = new SpringApplicationBuilder(TestApplication.class)
				.profiles("dev");

		try (var app = builder.run()) {
			Assertions.assertThat(app.getBean(AnTestBean.class)).isNotNull();
			Assertions.assertThatThrownBy(() -> app.getBean(ArticleRepository.class))
					.isInstanceOf(NoSuchBeanDefinitionException.class);
		}
	}

	/**
	 * 验证可以使用多个配置源来创建应用上下文。
	 */
	@Test
	void testMultiSource() throws IOException, InterruptedException {
		var builder = new SpringApplicationBuilder()
				.sources(TestApplication.class, ServiceApplication.class)
				.profiles("dev");

		try (var ignore = builder.run()) {
			var client = HttpClient.newHttpClient();
			var resp = client.send(HttpRequest
					.newBuilder(URI.create("https://localhost:2375/articles")).build(), BodyHandlers.ofString());
			Assertions.assertThat(resp.body()).isNotEmpty();
		}
	}
}
