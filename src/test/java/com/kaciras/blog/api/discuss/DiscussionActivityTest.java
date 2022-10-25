package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.notice.MailService;
import com.kaciras.blog.api.user.User;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class DiscussionActivityTest {

	private final MailService mailService = mock(MailService.class);

	private DiscussionActivity create(String uEmail, String pEmail) {
		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		return a;
	}

	@Test
	void sendReplyMail() {
		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		a.setEmail(null);
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService).send(eq("alice@example.com"), any(), any());
	}

	@Test
	void sendReplyMail2() {
		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		a.setEmail("bob@foo.com");
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService).send(eq("alice@example.com"), any(), any());
	}

	@Test
	void doNotSendWithoutEmail() {
		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}

	@Test
	void doNotSendSelfWithSameUser() {
		var user = new User();
		user.setEmail("alice@example.com");

		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		a.setUser(user);
		a.setParentUser(user);

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}

	@Test
	void doNotSendSelfWithEmail() {
		var a = new DiscussionActivity();
		a.setTitle("Test Title");
		a.setPreview("Activity content");
		a.setEmail("alice@example.com");
		a.setParentEmail("alice@example.com");

		a.sendMail(false, mailService);
		verify(mailService, never()).send(any(), any(), any());
	}
}
