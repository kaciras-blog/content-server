package com.kaciras.blog.api.user;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.principal.WebPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class SelfControllerTest extends AbstractControllerTest {

	@MockBean
	private UserRepository repository;

	@BeforeEach
	void setUp() {
		when(repository.get(eq(0))).thenReturn(User.GUEST);
	}

	@Test
	void getNonExists() throws Exception {
		mockMvc.perform(get("/user")).andExpect(status().is(200)).andExpect(snapshot.matchBody());
	}

	@Test
	void logout() throws Exception {
		mockMvc.perform(delete("/user")).andExpect(status().is(205));
	}

	@Test
	void updateNonExists() throws Exception {
		var data = new UpdateDTO("bob", ImageReference.parse("bob.png"), "bob@example.com");
		mockMvc.perform(patch("/user").content(toJson(data))).andExpect(status().is(403));
	}

	@Test
	void update() throws Exception {
		var userStub = new User();
		userStub.setName("alice");
		userStub.setAvatar(ImageReference.parse("alice.png"));
		userStub.setAuth(AuthType.LOCAL);
		userStub.setCreateTime(Instant.EPOCH);

		when(repository.get(eq(666))).thenReturn(userStub);

		var data = new UpdateDTO("bob", ImageReference.parse("bob.png"), "bob@example.com");
		var request = patch("/user")
				.principal(new WebPrincipal(666))
				.content(toJson(data));
		mockMvc.perform(request).andExpect(status().is(204));

		verify(repository).update(eq(userStub));
		assertThat(userStub.getName()).isEqualTo("bob");
		assertThat(userStub.getAvatar()).isEqualTo(ImageReference.parse("bob.png"));
		assertThat(userStub.getEmail()).isEqualTo("bob@example.com");
	}
}
