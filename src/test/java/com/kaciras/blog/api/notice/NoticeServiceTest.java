package com.kaciras.blog.api.notice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

@ActiveProfiles("test")
@SpringBootTest
final class NoticeServiceTest {

	@MockBean
	private ThreadPoolTaskScheduler executor;

	@MockBean
	private MailService mailService;

	@Autowired
	private NoticeService service;

	@Autowired
	private RedisConnectionFactory redis;

	@BeforeEach
	void setUp() {
		redis.getConnection().flushDb();
		when(executor.submit(any(Callable.class))).then(i -> i.<Callable>getArgument(0).call());
	}

	@Test
	void asyncReport() {
		clearInvocations(executor); // TODO: 每次测试都加载全部bean很烦人，要重构下

		service.notify(new TestActivity(666));
		verify(executor).submit(any(Callable.class));
	}

	@Test
	void sendMail() {
		clearInvocations(executor);

		service.notify(new TestActivity2());
		service.notify(new TestActivity(666));
		service.notify(new TestActivity(666));

		verify(mailService).sendToAdmin(eq("title"), eq("content"));
		verify(mailService, noMoreInteractions()).sendToAdmin(any(), any());
	}

	@Test
	void getAll() {
		service.notify(new TestActivity(666));

		var list = service.getAll();

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getType()).isEqualTo(ActivityType.Discussion);
		assertThat(list.get(0).getTime()).isNotNull();
		assertThat(list.get(0).getData()).isNotNull();
	}

	@Test
	void clearAll() {
		service.notify(new TestActivity(666));

		service.clear();

		assertThat(service.getAll()).isEmpty();
	}
}
