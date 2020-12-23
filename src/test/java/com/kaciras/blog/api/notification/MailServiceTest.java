package com.kaciras.blog.api.notification;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

final class MailServiceTest {

	private final JavaMailSender mockSender = Mockito.mock(JavaMailSender.class);

	@Test
	void send() throws Exception {
		Mockito.when(mockSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());

		var service = new MailService(mockSender, "test@example.com");

		var entry = new DiscussionActivity();
		entry.setTitle("Test Article");
		entry.setPreview("test content preview");
		entry.setTime(Instant.now());
		entry.setUrl("https://example.com");
		entry.setTopicFloor(7);
		entry.setReplyFloor(1);

		service.send("test@example.com", "网吧充钱提醒", "您有新的消息请注意查收");

		var c = ArgumentCaptor.forClass(MimeMessage.class);
		Mockito.verify(mockSender).send(c.capture());
		assertThat(c.getValue().getContent().toString()).contains("您有新的消息请注意查收");
	}
}
