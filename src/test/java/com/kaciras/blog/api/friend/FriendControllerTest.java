package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static com.kaciras.blog.api.friend.TestHelper.createFriend;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(FriendController.class)
final class FriendControllerTest extends AbstractControllerTest {

	@MockitoBean
	private FriendRepository repository;

	@MockitoBean
	private FriendValidateService validateService;

	@Test
	void getFriends() throws Exception {
		var result = new FriendLink[]{
				createFriend("A"),
				createFriend("B"),
				createFriend("C"),
		};
		when(repository.getAll()).thenReturn(result);

		var exchange = mockMvc.perform(get("/friends"))
				.andExpect(status().is(200))
				.andReturn();

		List<FriendLink> list = objectMapper.readValue(exchange.getResponse().getContentAsByteArray(),
				objectMapper.getTypeFactory().constructCollectionType(List.class, FriendLink.class));

		assertThat(list).hasSize(3);
		assertThat(list.get(0)).usingRecursiveComparison().isEqualTo(result[0]);
		assertThat(list.get(1)).usingRecursiveComparison().isEqualTo(result[1]);
		assertThat(list.get(2)).usingRecursiveComparison().isEqualTo(result[2]);
	}

	private static Stream<Arguments> invalidFields() {
		return Stream.of(
				Arguments.of("url", null),
				Arguments.of("url", URI.create("ftp://test")),
				Arguments.of("url", URI.create("")),

				Arguments.of("name", null),
				Arguments.of("name", ""),
				Arguments.of("name", "toooooooooooooooolong"),

				Arguments.of("background", null)
		);
	}

	@MethodSource("invalidFields")
	@ParameterizedTest
	void addInvalid(String field, Object value) throws Exception {
		var friend = mutate(createFriend("example.com"), field, value);
		var request = post("/friends").content(toJson(friend));
		mockMvc.perform(request).andExpect(status().is(400));
	}

	@Test
	void add() throws Exception {
		when(repository.add(any())).thenReturn(true);

		var request = post("/friends")
				.principal(ADMIN)
				.content(toJson(createFriend("example.com")));

		mockMvc.perform(request)
				.andExpect(status().is(201))
				.andExpect(header().string("Location", "/friends/example.com"));

		verify(repository).add(any());
		verify(validateService).addForValidate(any());
	}

	@Test
	void repeatAdd() throws Exception {
		when(repository.add(any())).thenReturn(false);

		var request = post("/friends")
				.principal(ADMIN)
				.content(toJson(createFriend("example.com")));
		mockMvc.perform(request)
				.andExpect(status().is(409));

		verify(validateService, never()).addForValidate(any());
	}

	@Test
	void rupture() throws Exception {
		when(repository.remove(any())).thenReturn(true);

		mockMvc.perform(delete("/friends/example.com").principal(ADMIN))
				.andExpect(status().is(200));

		verify(repository).remove(eq("example.com"));
		verify(validateService).removeFromValidate(eq("example.com"));
	}

	@Test
	void ruptureNonExists() throws Exception {
		when(repository.remove(any())).thenReturn(false);

		mockMvc.perform(delete("/friends/example.com").principal(ADMIN))
				.andExpect(status().is(404));
	}

	@Test
	void update() throws Exception {
		when(repository.update(any(), any())).thenReturn(true);

		var friend = toJson(createFriend("example.com"));
		mockMvc.perform(put("/friends/example.com")
				.content(friend)
				.principal(ADMIN))
				.andExpect(status().is(200));
	}

	@Test
	void updateNonExists() throws Exception {
		when(repository.update(any(), any())).thenReturn(false);

		var friend = toJson(createFriend("example.com"));
		mockMvc.perform(put("/friends/example.com")
				.content(friend)
				.principal(ADMIN))
				.andExpect(status().is(404));

		verify(validateService, never()).addForValidate(any());
		verify(validateService, never()).removeFromValidate(any());
	}

	@Test
	void sort() throws Exception {
		mockMvc.perform(put("/friends")
				.principal(ADMIN)
				.content(toJson(new String[]{"C", "A", "B"})))
				.andExpect(status().is(200));

		verify(repository).updateSort(new String[]{"C", "A", "B"});
	}
}
