package net.kaciras.blog.api.article;

import net.kaciras.blog.api.AbstractSpringTest;
import net.kaciras.blog.api.draft.DraftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ArticleControllerTest extends AbstractSpringTest {

	@MockBean
	private ArticleRepository repository;

	@MockBean
	private DraftRepository draftRepository;

	@MockBean
	private ArticleMapper articleMapper;

	@MockBean
	private ArticleManager articleManager;

	private Article article = spy(new Article());

	@BeforeEach
	void setUp() {
		article.setId(5);
		when(repository.get(5)).thenReturn(article);
	}

	@Test
	void handlePatch() throws Exception {
		Mockito.doNothing().when(article).updateCategory(2);

		mockMvc.perform(patch("/articles/5")
				.principal(ADMIN)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{ \"category\": 2 }"))
				.andExpect(status().is(200));

		verify(article).updateCategory(2);
		verify(article, times(0)).updateDeleted(anyBoolean());
		verify(article, times(0)).updateUrlTitle(anyString());
	}
}
