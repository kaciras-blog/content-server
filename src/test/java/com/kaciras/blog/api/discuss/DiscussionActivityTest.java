package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.notice.MailService;
import com.kaciras.blog.api.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class DiscussionActivityTest {

	private final MailService mailService = mock(MailService.class);

	@BeforeEach
	void setUp() {
		doReturn("_HTML_").when(mailService).interpolate(any(), any());
	}

	private DiscussionActivity create() {
		var a = new DiscussionActivity();
		a.setUrl("https://example.com");
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		a.setUser(User.GUEST);
		a.setParentUser(User.GUEST);
		return a;
	}

	@Test
	void sendReplyMail() {
		var a = create();
		a.setEmail(null);
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService).send(eq("alice@example.com"), anyString(), anyString());
	}

	@Test
	void sendReplyMail2() {
		var a = create();
		a.setEmail("bob@foo.com");
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService).send(eq("alice@example.com"), anyString(), anyString());
	}

	@Test
	void doNotSendWithoutEmail() {
		var a = create();

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}

	@Test
	void doNotSendSelfWithSameUser() {
		var user = new User();
		user.setEmail("alice@example.com");

		var a = create();
		a.setUser(user);
		a.setParentUser(user);

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}

	@Test
	void doNotSendSelfWithEmail() {
		var a = create();
		a.setEmail("alice@example.com");
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}
}
