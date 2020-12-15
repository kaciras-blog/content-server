package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.AbstractControllerTest;
import com.kaciras.blog.infra.codec.ImageReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

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

final class FriendControllerTest extends AbstractControllerTest {

	@MockBean
	private FriendRepository repository;

	@MockBean
	private FriendValidateService validateService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getFriends() throws Exception {
		var result = new FriendLink[]{
				createFriend("A"),
				createFriend("B"),
				createFriend("C"),
		};
		when(repository.getFriends()).thenReturn(result);

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

	private static Stream<Arguments> invalidFriends() {
		var url = URI.create("https://example.com");
		var image = ImageReference.parse("test.png");
		return Stream.of(
				Arguments.of(new FriendLink(null, "test", image, image, null, null)),
				Arguments.of(new FriendLink(URI.create(""), "test", image, image, null, null)),
				Arguments.of(new FriendLink(URI.create("ftp://test"), "test", image, image, null, null)),
				Arguments.of(new FriendLink(url, null, image, image, null, null)),
				Arguments.of(new FriendLink(url, "", image, image, null, null)),
				Arguments.of(new FriendLink(url, "123456789123456789", image, image, null, null)),
				Arguments.of(new FriendLink(url, "test", null, image, null, null)),
				Arguments.of(new FriendLink(url, "test", image, null, null, null))
		);
	}

	@MethodSource("invalidFriends")
	@ParameterizedTest
	void invalidFriendLink(FriendLink value) throws Exception {
		var request = post("/friends").content(objectMapper.writeValueAsBytes(value));
		mockMvc.perform(request).andExpect(status().is(400));
	}

	@Test
	void add() throws Exception {
		when(repository.addFriend(any())).thenReturn(true);

		var request = post("/friends")
				.principal(ADMIN)
				.content(objectMapper.writeValueAsString(createFriend("example.com")));
		mockMvc.perform(request)
				.andExpect(status().is(201))
				.andExpect(header().string("Location", "/friends/example.com"));

		verify(repository).addFriend(any());
		verify(validateService).addForValidate(any());
	}

	@Test
	void repeatAdd() throws Exception {
		when(repository.addFriend(any())).thenReturn(false);

		var request = post("/friends")
				.principal(ADMIN)
				.content(objectMapper.writeValueAsString(createFriend("example.com")));
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
		when(repository.updateFriend(any(), any())).thenReturn(true);

		var friend = objectMapper.writeValueAsString(createFriend("test"));
		mockMvc.perform(put("/friends/example.com")
				.content(friend)
				.principal(ADMIN))
				.andExpect(status().is(200));
	}

	@Test
	void updateNonExists() throws Exception {
		when(repository.updateFriend(any(), any())).thenReturn(false);

		var friend = objectMapper.writeValueAsString(createFriend("test"));
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
				.content(objectMapper.writeValueAsBytes(new String[]{"C", "A", "B"})))
				.andExpect(status().is(200));

		verify(repository).updateSort(new String[]{"C", "A", "B"});
	}
}
