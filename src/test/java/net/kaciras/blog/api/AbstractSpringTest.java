package net.kaciras.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.infra.principal.ServletSecurityContextFilter;
import net.kaciras.blog.infra.principal.WebPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Transactional
@ActiveProfiles("test") // 这个放在测试的Application类上无效
@SpringBootTest
public abstract class AbstractSpringTest {

	public static final WebPrincipal ADMIN = new WebPrincipal(WebPrincipal.ADMIN_ID);
	public static final WebPrincipal ANONYMOUS = new WebPrincipal(WebPrincipal.ANONYMOUS_ID);

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
}
