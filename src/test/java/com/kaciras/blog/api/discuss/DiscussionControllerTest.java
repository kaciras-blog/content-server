package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.article.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class DiscussionControllerTest extends AbstractControllerTest {

	@MockBean
	private ArticleRepository repository;

	@MockBean
	private DiscussionService service;

	@MockBean
	private ViewModelMapper draftRepository;

	@Test
	void testPost() throws Exception {
		mockMvc.perform(post("/discussions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"content\": \"test\", \"nickname\": \"12345678901234567\" }"))
				.andExpect(status().is(400));
	}
}
