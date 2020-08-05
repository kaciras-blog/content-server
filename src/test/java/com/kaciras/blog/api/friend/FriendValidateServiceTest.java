package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.kaciras.blog.api.friend.TestHelper.createFriend;

@Import({
		KxCodecAutoConfiguration.class,
		RedisAutoConfiguration.class,
		JacksonAutoConfiguration.class,
})
@ActiveProfiles("test")
@SpringBootTest
final class FriendValidateServiceTest {

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private FriendRepository repository;

	@MockBean
	private HttpClient httpClient;

	@Autowired
	private FriendValidateService service;

	@Autowired
	private RedisConnectionFactory redis;

	@BeforeEach
	void flushDb() {
		redis.getConnection().flushDb();
	}

	@Test
	void nk() throws Exception {
		service.addForValidate(createFriend("example.com", null, Instant.MIN));

		var response = (HttpResponse<Object>) Mockito.mock(HttpResponse.class);
		Mockito.when(response.statusCode()).thenReturn(200);
		Mockito.when(response.body()).thenReturn("");
		var rv = CompletableFuture.completedFuture(response);
		Mockito.when(httpClient.sendAsync(Mockito.any(), Mockito.any())).thenReturn(rv);

		service.startValidation();
	}
}
