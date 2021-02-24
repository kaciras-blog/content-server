package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.SessionValue;
import com.kaciras.blog.api.account.SessionService;
import com.kaciras.blog.api.user.UserManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
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

final class AccountControllerTest extends AbstractControllerTest {

	@MockBean
	private AccountRepository repository;

	@MockBean
	private UserManager userManager;

	@MockBean
	private SessionService sessionService;

	private static Stream<Arguments> invalidRegisterDTOs() {
		var chs = new char[2000];
		Arrays.fill(chs, 'p');
		var longText = new String(chs);
		return Stream.of(
				Arguments.of(new SignupDTO(null, "foobar2000", "6666")),
				Arguments.of(new SignupDTO("alice", null, "6666")),
				Arguments.of(new SignupDTO("alice", "foobar2000", null)),

				Arguments.of(new SignupDTO("", "foobar2000", "6666")),
				Arguments.of(new SignupDTO(longText, "foobar2000", "6666")),
				Arguments.of(new SignupDTO("♜♝♞♟", "foobar2000", "6666")),

				Arguments.of(new SignupDTO("alice", "1", "6666")),
				Arguments.of(new SignupDTO("alice", longText, "6666"))
		);
	}

	@MethodSource("invalidRegisterDTOs")
	@ParameterizedTest
	void invalidSignupRequest(SignupDTO dto) throws Exception {
		var session = new MockHttpSession();
		SessionValue.CAPTCHA.setTo(session, "6666");
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.now());

		mockMvc.perform(post("/accounts").content(toJson(dto)).session(session))
				.andExpect(status().is(400));
	}

	@Test
	void signupWithoutCaptcha() throws Exception {
		var dto = new SignupDTO("alice", "foobar2000", "6666");
		mockMvc.perform(post("/accounts").content(toJson(dto)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.message").value("验证码错误"));
	}

	@Test
	void wrongCaptcha() throws Exception {
		var dto = new SignupDTO("alice", "foobar2000", "6666");
		var session = new MockHttpSession();
		SessionValue.CAPTCHA.setTo(session, "6666");
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.EPOCH);

		mockMvc.perform(post("/accounts").content(toJson(dto)).session(session))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.message").value("验证码已过期，请重试"));
	}

	@Test
	void nameConflict() throws Exception {
		when(userManager.createNew(any(), any(), any())).thenThrow(new DuplicateKeyException(""));

		var dto = new SignupDTO("alice", "foobar2000", "6666");
		var session = new MockHttpSession();
		SessionValue.CAPTCHA.setTo(session, "6666");
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.now());

		mockMvc.perform(post("/accounts").content(toJson(dto)).session(session))
				.andExpect(status().is(400));
	}

	@Test
	void signup() throws Exception {
		when(userManager.createNew(any(), any(), any())).thenReturn(1);

		var dto = new SignupDTO("alice", "foobar2000", "6666");
		var session = new MockHttpSession();
		SessionValue.CAPTCHA.setTo(session, "6666");
		SessionValue.CAPTCHA_TIME.setTo(session, Instant.now());

		mockMvc.perform(post("/accounts").content(toJson(dto)).session(session))
				.andExpect(status().is(201));

		verify(sessionService).putUser(notNull(), notNull(), eq(1), eq(true));
	}

	@Test
	void loginNonExists() throws Exception {
		when(repository.findByName(any())).thenReturn(null);

		var dto = new LoginDTO("alice", "foobar2000", false);
		mockMvc.perform(post("/accounts/login").content(toJson(dto)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.message").value("密码错误或用户不存在"));
	}

	@Test
	void invalidPassword() throws Exception {
		var account = Account.create(1, "alice", "foobar2000");
		when(repository.findByName(any())).thenReturn(account);

		var dto = new LoginDTO("alice", "12345678", false);
		mockMvc.perform(post("/accounts/login").content(toJson(dto)))
				.andExpect(status().is(400))
				.andExpect(jsonPath("$.message").value("密码错误或用户不存在"));
	}

	@Test
	void login() throws Exception {
		var account = Account.create(1, "alice", "foobar2000");
		when(repository.findByName(any())).thenReturn(account);

		var dto = new LoginDTO("alice", "foobar2000", false);
		mockMvc.perform(post("/accounts/login").content(toJson(dto)))
				.andExpect(status().is(201));

		verify(sessionService).putUser(notNull(), notNull(), eq(1), eq(false));
	}
}
