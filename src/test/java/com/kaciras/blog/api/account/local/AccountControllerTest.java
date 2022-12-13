package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.SessionValue;
import com.kaciras.blog.api.account.SessionService;
import com.kaciras.blog.api.user.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockHttpSession;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({AccountController.class})
final class AccountControllerTest extends AbstractControllerTest {

	@MockBean
	private AccountRepository repository;

	@MockBean
	private UserManager userManager;

	@MockBean
	private SessionService sessionService;

	private final MockHttpSession session = new MockHttpSession();
	private final SignupDTO signupDTO = new SignupDTO("alice", "foobar2000", "6666");
	private final LoginDTO loginDTO = new LoginDTO("alice", "foobar2000", false);

	@BeforeEach
	void beforeEach() {
		SessionValue.CAPTCHA.setTo(session, "6666");
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.now());
	}

	private static Stream<Arguments> invalidSignupFields() {
		var chs = new char[2000];
		Arrays.fill(chs, 'p');
		var longText = new String(chs);

		return Stream.of(
				Arguments.of("name", null),
				Arguments.of("password", null),
				Arguments.of("captcha", null),

				Arguments.of("name", ""),
				Arguments.of("name", longText),
				Arguments.of("name", "♜♝♞♟"),

				Arguments.of("password", "1"),
				Arguments.of("password", longText)
		);
	}

	@MethodSource("invalidSignupFields")
	@ParameterizedTest
	void invalidSignupRequest(String field, Object value) throws Exception {
		var body = mutate(signupDTO, field, value);

		mockMvc.perform(post("/accounts").content(toJson(body)).session(session))
				.andExpect(status().is(400));
	}

	@Test
	void signupWithoutCaptcha() throws Exception {
		mockMvc.perform(post("/accounts").content(toJson(signupDTO)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").value("验证码错误"));
	}

	@Test
	void wrongCaptcha() throws Exception {
		SessionValue.CAPTCHA.setTo(session, "1111");

		mockMvc.perform(post("/accounts").content(toJson(signupDTO)).session(session))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").value("验证码错误"));
	}

	@Test
	void expiredCaptcha() throws Exception {
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.EPOCH);

		mockMvc.perform(post("/accounts").content(toJson(signupDTO)).session(session))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").value("验证码已过期，请重试"));
	}

	@Test
	void nameConflict() throws Exception {
		when(userManager.createNew(any(), any(), any())).thenThrow(new DuplicateKeyException(""));

		mockMvc.perform(post("/accounts").content(toJson(signupDTO)).session(session))
				.andExpect(status().is(400));
	}

	@Test
	void signup() throws Exception {
		when(userManager.createNew(any(), any(), any())).thenReturn(1);

		mockMvc.perform(post("/accounts").content(toJson(signupDTO)).session(session))
				.andExpect(status().is(201));

		verify(sessionService).putUser(notNull(), eq(1), eq(true));
	}

	@Test
	void loginNonExists() throws Exception {
		when(repository.findByName(any())).thenReturn(null);

		mockMvc.perform(post("/accounts/login").content(toJson(loginDTO)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").value("密码错误或用户不存在"));
	}

	@Test
	void invalidPassword() throws Exception {
		var account = Account.create(1, "alice", "12345678");
		when(repository.findByName(any())).thenReturn(account);

		mockMvc.perform(post("/accounts/login").content(toJson(loginDTO)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.detail").value("密码错误或用户不存在"));
	}

	@Test
	void login() throws Exception {
		var account = Account.create(1, "alice", "foobar2000");
		when(repository.findByName(any())).thenReturn(account);

		mockMvc.perform(post("/accounts/login").content(toJson(loginDTO)))
				.andExpect(status().is(201));

		verify(sessionService).putUser(notNull(), eq(1), eq(false));
	}
}
