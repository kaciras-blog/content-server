package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.infra.principal.ServletSecurityContextFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static net.kaciras.blog.api.AbstractSpringTest.ANONYMOUS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
final class ControllerTest {

	@MockBean
	private DiscussionService discussionService;

	protected MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext wac;

	@Autowired
	protected ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac)
				.addFilter(new ServletSecurityContextFilter())
				.defaultRequest(get("/")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.principal(ANONYMOUS))
				.build();
	}

	@Test
	void test() throws Exception {
		var request = get("/discussions")
				.param("objectId", "37")
				.param("start", "0")
				.param("count", "20");

		mockMvc.perform(request)
				.andExpect(status().isOk());
	}
}
