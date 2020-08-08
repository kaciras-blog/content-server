package com.kaciras.blog.api.notification;

import com.kaciras.blog.api.discuss.Discussion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
final class NotificationServiceTest {

	@MockBean
	private ThreadPoolTaskScheduler executor;

	@Autowired
	private NotificationService service;

	@Test
	void asyncReport() {
		clearInvocations(executor); // TODO: 每次测试都加载全部bean很烦人

		service.reportDiscussion(new Discussion(), null, null);
		verify(executor).submit(any(Callable.class));
	}
}
