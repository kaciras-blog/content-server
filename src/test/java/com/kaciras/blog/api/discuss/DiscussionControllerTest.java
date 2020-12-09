package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class DiscussionControllerTest extends AbstractControllerTest {

	@MockBean
	private ChannelRegistration channels;

	@MockBean
	private DiscussionRepository repository;

	@MockBean
	private UserManager userManager;

	@MockBean
	private NotificationService notification;

	@Autowired
	private ObjectMapper objectMapper;

	private static Stream<Arguments> invalidPostRequests() {
		return Stream.of(
				Arguments.of("{ \"content\": \" \" }"),
				Arguments.of("{}"),
				Arguments.of("{ \"content\": \"test\", \"nickname\": \"12345678901234567\" }"),
				Arguments.of("{ \"content\": \"test\", \"nickname\": \"\" }"),
				Arguments.of("{ \"content\": \"test\", \"nickname\": \"  \" }")
		);
	}

	@MethodSource("invalidPostRequests")
	@ParameterizedTest
	void postWithInvalidField(String body) throws Exception {
		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void postWithNonExistsChannel() throws Exception {
		var input = new PublishInput(0, 0, 0, 0, null, "test");
		var body = objectMapper.writeValueAsString(input);

		when(channels.getChannel(anyInt(), anyInt())).thenThrow(new RequestArgumentException());
		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void postWithNonExistsParent() throws Exception {
		var input = new PublishInput(0, 0, 1, 0, null, "test");
		var body = objectMapper.writeValueAsString(input);

		when(repository.get(anyInt())).thenReturn(Optional.empty());
		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void publish() throws Exception {
		var input = new PublishInput(0, 0, 0, 0, null, "test");
		var body = objectMapper.writeValueAsString(input);

		var channel = new DiscussChannel("ch", "http://abc");
		when(channels.getChannel(anyInt(), anyInt())).thenReturn(channel);

		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(201));

		var c = ArgumentCaptor.forClass(Discussion.class);
		verify(repository).add(c.capture());
		verify(notification).reportDiscussion(any(), eq(null), channel);

		assertThat(c.getValue().getContent()).isEqualTo("test");
		assertThat(c.getValue().getAddress()).isNotNull();
		assertThat(c.getValue().getUserId()).isEqualTo(1);
		assertThat(c.getValue().getState()).isEqualTo(DiscussionState.Visible);
	}

	@Test
	void getList() throws Exception {
		var request = get("/discussions").param("parent", "1").param("count", "9999");
		mockMvc.perform(request).andExpect(status().is(400));
	}
}
