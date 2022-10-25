package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.api.notice.NoticeService;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVO;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;
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
	private NoticeService notification;

	@Autowired
	private DiscussionController controller;

	private final PublishDTO publishDTO = new PublishDTO(0, 0, 0, null,  null,"test content");

	@BeforeEach
	void setUp() {
		controller.setOptions(new DiscussionOptions());

		var topic = new Topic("TestTopic", "http://example.com");
		doReturn(topic).when(topics).get(anyInt(), anyInt());

		doReturn(new UserVO()).when(userManager).getUser(anyInt());
	}

	private static Stream<Arguments> invalidQueries() {
		return Stream.of(
				Arguments.of(get("/discussions"), 403),
				Arguments.of(get("/discussions").param("nestId", "0").param("state", "MODERATION"), 403),
				Arguments.of(get("/discussions").param("objectId", "1"), 403),
				Arguments.of(get("/discussions").param("type", "1"), 403),

				Arguments.of(get("/discussions").param("nestId", "0").param("childCount", "100"), 400),
				Arguments.of(get("/discussions").param("nestId", "0").param("count", "100"), 400)
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
		result.setType(1);
		result.setObjectId(5);
		result.setContent("评论内容，ID=" + id);
		result.setState(DiscussionState.VISIBLE);
		result.setTime(Instant.EPOCH);
		result.setAddress(InetAddress.getLoopbackAddress());
		return result;
	}

	@SuppressWarnings({"unchecked", "ConstantConditions"})
	@Test
	void getListWithChildren() throws Exception {
		var top = List.of(newItem(1, 0));
		var children = List.of(
				newItem(2, 1),
				newItem(3, 1)
		);
		when(repository.count(any())).thenReturn(top.size());
		when(repository.findAll(any())).thenReturn(top, children);

		var request = get("/discussions")
				.param("type", "0")
				.param("objectId", "1")
				.param("childCount", "5");

		mockMvc.perform(request)
				.andExpect(status().is(200))
				.andExpect(snapshot.matchBody());

		var firstQuery = new DiscussionQuery()
				.setType(0)
				.setObjectId(1)
				.setChildCount(5)
				.setPageable(PageRequest.of(0, 20));
		verify(repository).findAll(refEq(firstQuery));
		verify(repository).count(refEq(firstQuery));

		var secondQuery = new DiscussionQuery()
				.setNestId(1)
				.setPageable(PageRequest.of(0, 5));
		verify(repository).findAll(refEq(secondQuery));

		verifyNoMoreInteractions(repository);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void getListWithParent() throws Exception {
		var first = List.of(
				newItem(1, 0),
				newItem(2, 1),
				newItem(3, 4)
		);
		when(repository.count(any())).thenReturn(4);
		when(repository.findAll(any())).thenReturn(first);
		when(repository.get(anyCollection())).thenReturn(List.of(newItem(4, 0)));

		var request = get("/discussions")
				.param("type", "0")
				.param("objectId", "1")
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

		verify(repository).get(eq(Set.of(4)));
		verifyNoMoreInteractions(repository);
	}

	private static Stream<Arguments> invalidPostRequests() {
		var buf = new char[16384];
		Arrays.fill(buf, 'x');
		var longText = new String(buf);

		return Stream.of(
				Arguments.of("content", longText),
				Arguments.of("content", null),
				Arguments.of("content", ""),

				Arguments.of("nickname", "12345678901234567"),
				Arguments.of("nickname", ""),
				Arguments.of("nickname", "	   	"),

				Arguments.of("email", longText + "@qq.com"),
				Arguments.of("email", "NotAnEmailAddress")
		);
	}

	@MethodSource("invalidPostRequests")
	@ParameterizedTest
	void invalidPublish(String field, Object value) throws Exception {
		var body = toJson(mutate(publishDTO, field, value));
		mockMvc.perform(post("/discussions").content(body)).andExpect(status().is(400));
	}

	@Test
	void publishToNonExistsTopic() throws Exception {
		// 对与 spy 的对象，且方法内会抛异常，必须使用 doXX.when(obj).call 方式而不是 when(obj.call).thenXX
		doThrow(new RequestArgumentException()).when(topics).get(anyInt(), anyInt());

		mockMvc.perform(post("/discussions").content(toJson(publishDTO))).andExpect(status().is(400));
		verify(repository, noInteractions()).add(any());
	}

	@Test
	void publishToNonExistsParent() throws Exception {
		when(repository.get(anyInt())).thenReturn(Optional.empty());
		var dto = mutate(publishDTO, "parent", 1);

		mockMvc.perform(post("/discussions").content(toJson(dto))).andExpect(status().is(400));
	}

	@Test
	void publishToDetachedParent() throws Exception {
		when(repository.get(anyInt())).thenReturn(Optional.of(newItem(1, 0)));
		doThrow(new RequestArgumentException()).when(topics).get(anyInt(), anyInt());

		var dto = mutate(publishDTO, "parent", 1);

		mockMvc.perform(post("/discussions").content(toJson(dto))).andExpect(status().is(400));
		verify(repository, never()).add(any());
	}

	@Test
	void publish() throws Exception {
		mockMvc.perform(post("/discussions").content(toJson(publishDTO))).andExpect(status().is(201));

		var captor = ArgumentCaptor.forClass(Discussion.class);
		verify(repository).add(captor.capture());

		var stored = captor.getValue();
		assertThat(stored.getContent()).isEqualTo("test content");
		assertThat(stored.getAddress()).isNotNull();
		assertThat(stored.getUserId()).isEqualTo(0);
		assertThat(stored.getState()).isEqualTo(DiscussionState.VISIBLE);
	}

	@Test
	void notifyPublish() throws Exception {
		Answer<Void> mockAdd = i -> {
			var v = i.getArgument(0, Discussion.class);
			v.setNestId(1);
			v.setFloor(2);
			v.setNestFloor(3);
			return null;
		};
		doAnswer(mockAdd).when(repository).add(any());

		mockMvc.perform(post("/discussions").content(toJson(publishDTO)));

		verify(notification).notify(snapshot.matchArg());
	}

	@Test
	void optionDisabled() throws Exception {
		var options = new DiscussionOptions();
		options.disabled = true;
		controller.setOptions(options);

		mockMvc.perform(post("/discussions").content(toJson(publishDTO))).andExpect(status().is(403));
	}

	@Test
	void optionLoginRequired() throws Exception {
		var options = new DiscussionOptions();
		options.loginRequired = true;
		controller.setOptions(options);

		var request = post("/discussions").content(toJson(publishDTO));
		mockMvc.perform(request).andExpect(status().is(403));
		mockMvc.perform(request.principal(LOGINED)).andExpect(status().is(201));
	}

	@Test
	void optionModeration() throws Exception {
		var options = new DiscussionOptions();
		options.moderation = true;
		controller.setOptions(options);

		mockMvc.perform(post("/discussions").content(toJson(publishDTO))).andExpect(status().is(201));

		var captor = ArgumentCaptor.forClass(Discussion.class);
		verify(repository).add(captor.capture());
		assertThat(captor.getValue().getState()).isEqualTo(DiscussionState.MODERATION);
	}

	@Test
	void updateStateWithoutPermission() throws Exception {
		var request = patch("/discussions")
				.content("{ \"ids\": [1,2], \"state\": \"VISIBLE\" }");
		mockMvc.perform(request).andExpect(status().is(403));
	}

	@Test
	void updateState() throws Exception {
		var request = patch("/discussions")
				.principal(ADMIN)
				.content("{ \"ids\": [1,2], \"state\": \"VISIBLE\" }");

		mockMvc.perform(request).andExpect(status().is(204));

		verify(repository).updateState(eq(1), eq(DiscussionState.VISIBLE));
		verify(repository).updateState(eq(2), eq(DiscussionState.VISIBLE));
		verify(repository, noMoreInteractions()).updateState(anyInt(), any());
	}
}
