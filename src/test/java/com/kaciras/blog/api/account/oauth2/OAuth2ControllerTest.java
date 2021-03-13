package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.api.user.User;
import com.kaciras.blog.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = OAuth2ControllerTest.TestConfig.class)
final class OAuth2ControllerTest extends AbstractControllerTest {

	@Component
	static final class TestOAuth2Client implements OAuth2Client {

		@Override
		public AuthType authType() {
			return AuthType.GITHUB;
		}

		@Override
		public UriComponentsBuilder uriTemplate() {
			return UriComponentsBuilder.fromUriString("https://example.com");
		}

		@Override
		public UserProfile authorize(AuthorizeRequest context) {
			return new ProfileStub();
		}
	}

	static class ProfileStub implements UserProfile {

		@Override
		public String id() {
			return "github_id";
		}

		@Override
		public String name() {
			return "alice";
		}

		@Override
		public String email() {
			return "alice@example.com";
		}

		@Override
		public String avatar() {
			return null;
		}
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		OAuth2Client client() {
			return new TestOAuth2Client();
		}
	}

	@MockBean
	private OAuth2DAO oAuth2DAO;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private Clock clock;

	@BeforeEach
	void setUp() {
		when(clock.instant()).thenReturn(Instant.EPOCH);
	}

	@Test
	void invalidProvider() throws Exception {
		mockMvc.perform(get("/oauth2/connect/invalid")).andExpect(status().isNotFound());
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void redirect() throws Exception {
		var url = mockMvc.perform(get("/oauth2/connect/github"))
				.andExpect(status().isFound())
				.andReturn()
				.getResponse()
				.getRedirectedUrl();

		assertThat(url).startsWith("https://example.com");

		var params = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
		assertThat(params.getFirst("state")).isNotEmpty();
		assertThat(params.getFirst("redirect_uri")).isEqualTo("http://localhost/oauth2/callback");
	}

	@Test
	void createOauth2Session() throws Exception {
		var session = mockMvc.perform(get("/oauth2/connect/github").param("ret", "/foobar"))
				.andExpect(status().isFound())
				.andReturn()
				.getRequest()
				.getSession();

		assertThat(session).isNotNull();

		var ctx = (OAuth2Context) session.getAttribute("OA");
		assertThat(ctx.time).isEqualTo(Instant.EPOCH);
		assertThat(ctx.state).isNotEmpty();
		assertThat(ctx.provider).isEqualTo("github");
		assertThat(ctx.returnUri).isEqualTo("/foobar");
	}

	@Test
	void invalidSession() throws Exception {
		var request = get("/oauth2/callback")
				.param("state", "TODO")
				.param("code", "123456");
		mockMvc.perform(request).andExpect(status().isForbidden());
	}

	@Test
	void invalidState() throws Exception {
		var data = new OAuth2Context("github", "bar", "eee", Instant.EPOCH);
		var session = new MockHttpSession();
		session.setAttribute("OA", data);

		var request = get("/oauth2/callback")
				.session(session)
				.param("state", "invalid")
				.param("code", "123456");

		mockMvc.perform(request).andExpect(status().isBadRequest());
		assertThat(session.getAttribute("OA")).isNull();
	}

	@Test
	void callback() throws Exception {
		var data = new OAuth2Context("github", "bar", null, Instant.EPOCH);
		var session = new MockHttpSession();
		session.setAttribute("OA", data);

		when(oAuth2DAO.select(any(), any())).thenReturn(5);

		var request = get("/oauth2/callback")
				.session(session)
				.param("state", "bar")
				.param("code", "123456");
		mockMvc.perform(request).andExpect(status().isOk());
	}

	@Test
	void returnToPreviousPage() throws Exception {
		var data = new OAuth2Context("github", "bar", "/foobar", Instant.EPOCH);
		var session = new MockHttpSession();
		session.setAttribute("OA", data);

		when(oAuth2DAO.select(any(), any())).thenReturn(5);

		var request = get("/oauth2/callback")
				.session(session)
				.param("state", "bar")
				.param("code", "123456");
		mockMvc.perform(request)
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("http://localhost/foobar"));
	}

	@Test
	void createNewUser() throws Exception {
		var data = new OAuth2Context("github", "bar", "/foobar", Instant.EPOCH);
		var session = new MockHttpSession();
		session.setAttribute("OA", data);

		when(oAuth2DAO.select(any(), any())).thenReturn(null);

		doAnswer(s -> {
			s.<User>getArgument(0).setId(5);
			return null;
		}).when(userRepository).add(any());

		var request = get("/oauth2/callback")
				.session(session)
				.param("state", "bar")
				.param("code", "123456");
		mockMvc.perform(request)
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("http://localhost/foobar"));

		verify(userRepository).add(notNull());
		verify(oAuth2DAO).insert(eq("github_id"), eq(AuthType.GITHUB), eq(5));
	}
}
