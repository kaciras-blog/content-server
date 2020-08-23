package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.notification.FriendAccident;
import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.kaciras.blog.api.friend.TestHelper.createFriend;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

@Import({
		KxCodecAutoConfiguration.class,
		RedisAutoConfiguration.class,
		JacksonAutoConfiguration.class,
})
@ActiveProfiles("test")
@SpringBootTest(properties = "app.origin=https://blog.example.com")
final class FriendValidateServiceTest {

	@MockBean
	private NotificationService notification;

	@MockBean
	private FriendRepository repository;

	@MockBean
	private Clock clock;

	@MockBean
	private FriendValidator validator;

	@Autowired
	private FriendValidateService service;

	@Autowired
	private RedisConnectionFactory redis;

	@BeforeEach
	void flushDb() {
		redis.getConnection().flushDb();
	}

	private FriendLink addRecord(String domain, String friendPage, Instant time) {
		when(clock.instant()).thenReturn(time);
		var friend = createFriend(domain, friendPage, time);
		service.addForValidate(friend);
		when(repository.findByHost(eq(domain))).thenReturn(friend);
		return friend;
	}

	private void setValidateResult(boolean alive, URI newUrl, String html) {
		when(validator.visit(any())).thenReturn(CompletableFuture
				.completedFuture(new FriendSitePage(alive, newUrl, null, html)));
	}

	@Test
	void failedCount() throws Exception {
		var friend = addRecord("example.com", null, Instant.EPOCH);
		setValidateResult(false, null, null);

		service.startValidation();

		verify(notification, noInteractions()).reportFriend(any(), any(), any(), any());
	}

	@Test
	void notAlive() throws Exception {
		var friend = addRecord("example.com", null, Instant.EPOCH);
		setValidateResult(false, null, null);

		service.startValidation();
		service.startValidation();
		service.startValidation();
		service.startValidation();

		verify(notification).reportFriend(eq(FriendAccident.Type.Inaccessible), eq(friend), any(), isNull());
	}
}
