package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.URI;
import java.util.List;

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
		assertThat(list.get(0)).isEqualToComparingFieldByField(result[0]);
		assertThat(list.get(1)).isEqualToComparingFieldByField(result[1]);
		assertThat(list.get(2)).isEqualToComparingFieldByField(result[2]);
	}

	@Test
	void invalidFriendLink() throws Exception {
		var friend = new FriendLink(URI.create("https://test"), "test", null, null, null, null);
		mockMvc
				.perform(post("/friends").content(objectMapper.writeValueAsBytes(friend)))
				.andExpect(status().is(400));
	}

	@Test
	void add() throws Exception {
		when(repository.addFriend(any())).thenReturn(true);
		var body = objectMapper.writeValueAsString(createFriend("example.com"));

		mockMvc.perform(post("/friends")
				.principal(ADMIN)
				.content(body))
				.andExpect(status().is(201))
				.andExpect(header().string("Location", "/friends/example.com"));

		verify(repository).addFriend(any());
		verify(validateService).addForValidate(any());
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
	void sort() throws Exception {
		mockMvc.perform(put("/friends")
				.principal(ADMIN)
				.content(objectMapper.writeValueAsBytes(new String[]{"C", "A", "B"})))
				.andExpect(status().is(200));

		verify(repository).updateSort(new String[]{"C", "A", "B"});
	}
}
