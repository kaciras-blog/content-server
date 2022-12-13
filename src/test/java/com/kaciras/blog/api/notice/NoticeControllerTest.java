package com.kaciras.blog.api.notice;

import com.kaciras.blog.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(NoticeController.class)

class NoticeControllerTest extends AbstractControllerTest {

	@MockBean
	private NoticeService service;

	private static Stream<Arguments> adminOnlyRequests() {
		return Stream.of(Arguments.of(get("/notifications")), Arguments.of(delete("/notifications")));
	}

	@MethodSource("adminOnlyRequests")
	@ParameterizedTest
	void permission(MockHttpServletRequestBuilder request) throws Exception {
		mockMvc.perform(request).andExpect(status().is(403));
	}

	@Test
	void getAll() throws Exception {
		var data = objectMapper.valueToTree(new TestActivity(666));
		var notice = new Notice(ActivityType.FRIEND, Instant.EPOCH, data);
		when(service.getAll()).thenReturn(List.of(notice, notice));

		var request = get("/notifications").principal(ADMIN);
		mockMvc.perform(request)
				.andExpect(status().is(200))
				.andExpect(snapshot.matchBody());
	}

	@Test
	void clear() throws Exception {
		var request = delete("/notifications").principal(ADMIN);

		mockMvc.perform(request).andExpect(status().is(204));

		verify(service, times(1)).clear();
	}
}
