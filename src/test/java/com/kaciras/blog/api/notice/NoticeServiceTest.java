package com.kaciras.blog.api.notice;

import com.kaciras.blog.api.MinimumSpringTest;
import com.kaciras.blog.api.UseBlogRedis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

@Import({NoticeService.class, NoticeConfiguration.class})
@UseBlogRedis
@MinimumSpringTest
final class NoticeServiceTest {

	@MockitoBean
	private ThreadPoolTaskScheduler executor;

	@MockitoBean
	private Clock clock;

	@MockitoBean
	private MailService mailService;

	@Autowired
	private NoticeService service;

	@Autowired
	private RedisConnectionFactory redis;

	// TODO: 每次测试都加载全部 bean 很烦人，要重构下
	@BeforeEach
	void setUp() {
		redis.getConnection().serverCommands().flushDb();
		clearInvocations(executor);

		when(clock.instant()).thenReturn(Instant.EPOCH);
		when(executor.submit(any(Callable.class))).then(i -> i.<Callable>getArgument(0).call());
	}

	@Test
	void asyncReport() {
		service.notify(new TestActivity(666));
		verify(executor).submit(any(Callable.class));
	}

	@Test
	void mailAdmin() {
		service.notify(new TestActivity(666));
		service.notify(new TestActivity2());
		service.notify(new TestActivity(666));

		verify(mailService).sendToAdmin(eq("title"), eq("content"));
		verify(mailService, noMoreInteractions()).sendToAdmin(any(), any());
	}

	@Test
	void mailAdminAfterSilentDuration() {
		service.notify(new TestActivity(666));

		when(clock.instant()).thenReturn(Instant.now());
		service.notify(new TestActivity(666));

		verify(mailService, times(2)).sendToAdmin(anyString(), anyString());
	}

	@Test
	void getAll() {
		service.notify(new TestActivity(666));

		var list = service.getAll();

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getType()).isEqualTo(ActivityType.DISCUSSION);
		assertThat(list.get(0).getTime()).isNotNull();
		assertThat(list.get(0).getData()).isNotNull();
	}

	@Test
	void clearAll() {
		service.notify(new TestActivity(666));

		service.clear();

		assertThat(service.getAll()).isEmpty();
	}

	@Test
	void messageAdmin() {
		service.notify(new TestActivity2());
		assertThat(service.getAll()).isEmpty();
	}
}
