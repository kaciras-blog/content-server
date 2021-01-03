package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.article.Article;
import com.kaciras.blog.api.article.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class TopicRegistrationTest {

	private final ArticleRepository articleRepository = mock(ArticleRepository.class);

	private final TopicRegistration registration = new TopicRegistration(articleRepository);

	@BeforeEach
	void setUp(){
		registration.setOrigin("https://blog.example.com");
	}

	@Test
	void getArticleTopic(){
		var article = new Article();
		article.setId(666);
		article.setTitle("Title of the article");
		article.setUrlTitle("url-title");
		when(articleRepository.findById(1)).thenReturn(article);

		var topic = registration.get(1, 1);
		assertThat(topic.getName()).isEqualTo("Title of the article");
		assertThat(topic.getUrl()).isEqualTo("https://blog.example.com/article/666/url-title");
	}

	@Test
	void getTopic() {
		var topic = registration.get(2, 1);
		assertThat(topic.getName()).isEqualTo("关于 - 博主");
		assertThat(topic.getUrl()).isEqualTo("https://blog.example.com/about/me");
	}
}
