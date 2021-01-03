package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.SnapshotAssertion;
import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVo;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.RequestBuilder;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class DiscussionControllerTest extends AbstractControllerTest {

	@SpyBean
	private TopicRegistration topics;

	@MockBean
	private DiscussionRepository repository;

	@MockBean
	private UserManager userManager;

	@MockBean
	private NotificationService notification;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SnapshotAssertion snapshot;

	private static Stream<Arguments> invalidQueries() {
		return Stream.of(
				Arguments.of(get("/discussions"), 403),
				Arguments.of(get("/discussions").param("parent", "0").param("childCount", "100"), 400),
				Arguments.of(get("/discussions").param("parent", "0").param("count", "100"), 400)
		);
	}

	@MethodSource("invalidQueries")
	@ParameterizedTest
	void getListWithInvalidQuery(RequestBuilder request, int code) throws Exception {
		mockMvc.perform(request).andExpect(status().is(code));
	}

	private Discussion newItem(int id, int parent) {
		var result = new Discussion();
		result.setId(id);
		result.setParent(parent);
		result.setObjectId(5);
		result.setContent("评论内容，ID=" + id);
		result.setState(DiscussionState.Visible);
		result.setTime(Instant.EPOCH);
		result.setAddress(InetAddress.getLoopbackAddress());
		return result;
	}

	@SuppressWarnings({"unchecked", "ConstantConditions"})
	@Test
	void getListWithChildren() throws Exception {
		when(userManager.getUser(anyInt())).thenReturn(new UserVo());

		var top = List.of(newItem(1, 0));
		var children = List.of(newItem(2, 1), newItem(3, 1));
		when(repository.count(any())).thenReturn(top.size());
		when(repository.findAll(any())).thenReturn(top, children);

		var request = get("/discussions")
				.param("type", "0")
				.param("objectId", "1")
				.param("count", "20")
				.param("childCount", "5");

		mockMvc.perform(request)
				.andExpect(status().is(200))
				.andExpect(snapshot.matchBody());

		var firstQuery = new DiscussionQuery()
				.setType(0)
				.setTopParent(0)
				.setChildCount(5)
				.setPageable(PageRequest.of(0, 20));
		verify(repository).findAll(refEq(firstQuery));
		verify(repository).count(refEq(firstQuery));

		var secondQuery = new DiscussionQuery()
				.setObjectId(1)
				.setPageable(PageRequest.of(0, 5));
		verify(repository).findAll(refEq(secondQuery));

		verifyNoMoreInteractions(repository);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void getListWithParent() throws Exception {
		when(userManager.getUser(anyInt())).thenReturn(new UserVo());

		var first = List.of(
				newItem(1, 0),
				newItem(2, 1),
				newItem(3, 4)
		);
		when(repository.count(any())).thenReturn(first.size());
		when(repository.findAll(any())).thenReturn(first);
		when(repository.get(eq(4))).thenReturn(Optional.of(newItem(4, 0)));

		var request = get("/discussions")
				.param("type", "0")
				.param("objectId", "1")
				.param("count", "20")
				.param("includeParent", "true");

		mockMvc.perform(request)
				.andExpect(status().is(200))
				.andExpect(snapshot.matchBody());

		var firstQuery = new DiscussionQuery()
				.setType(0)
				.setObjectId(1)
				.setIncludeParent(true)
				.setPageable(PageRequest.of(0, 20));
		verify(repository).findAll(refEq(firstQuery));
		verify(repository).count(refEq(firstQuery));

		verify(repository).get(eq(4));
		verifyNoMoreInteractions(repository);
	}

	private static Stream<Arguments> invalidPostRequests() {
		var longText = new char[20000];
		Arrays.fill(longText, '蛤');
		return Stream.of(
				Arguments.of("{ \"content\": \" \" }"),
				Arguments.of("{ \"content\": \"" + new String(longText) + "\" }"),
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
	void postWithNonExistsTopic() throws Exception {
		var input = new PublishInput(0, 0, 0, null, "test");
		var body = objectMapper.writeValueAsString(input);

		// 对与 spy 的对象，且方法内会抛异常，必须使用 doXX.when(obj).call 方式而不是 when(obj.call).thenXX
		doThrow(new RequestArgumentException()).when(topics).get(anyInt(), anyInt());

		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void postWithNonExistsParent() throws Exception {
		var input = new PublishInput(0, 0, 1, null, "test");
		var body = objectMapper.writeValueAsString(input);

		when(repository.get(anyInt())).thenReturn(Optional.empty());
		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void publish() throws Exception {
		var input = new PublishInput(0, 0, 0, null, "test");
		var body = objectMapper.writeValueAsString(input);

		var topic = new Topic("ch", "http://example.com");
		doReturn(topic).when(topics).get(anyInt(), anyInt());

		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(201));

		var captor = ArgumentCaptor.forClass(Discussion.class);
		verify(repository).add(captor.capture());
		verify(notification).reportDiscussion(any(), eq(null), eq(topic));

		var stored = captor.getValue();
		assertThat(stored.getContent()).isEqualTo("test");
		assertThat(stored.getAddress()).isNotNull();
		assertThat(stored.getUserId()).isEqualTo(0);
		assertThat(stored.getState()).isEqualTo(DiscussionState.Visible);
	}

	@Test
	void updateStateWithoutPermission() throws Exception {
		var request = patch("/discussions").content("{ \"ids\": [1,2], \"state\": \"Visible\" }");
		mockMvc.perform(request).andExpect(status().is(403));
	}

	@Test
	void updateState() throws Exception {
		var request = patch("/discussions")
				.principal(ADMIN)
				.content("{ \"ids\": [1,2], \"state\": \"Visible\" }");

		mockMvc.perform(request).andExpect(status().is(204));

		verify(repository).updateState(eq(1), eq(DiscussionState.Visible));
		verify(repository).updateState(eq(2), eq(DiscussionState.Visible));
		verify(repository, noMoreInteractions()).updateState(anyInt(), any());
	}
}
