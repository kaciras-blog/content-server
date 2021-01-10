package com.kaciras.blog.api.notice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

final class MailServiceTest {

	private final JavaMailSender mockSender = Mockito.mock(JavaMailSender.class);

	private final MailService service = new MailService(mockSender, "alice@example.com", "bob@example.com");

	@BeforeEach
	void setUp() {
		Mockito.when(mockSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
	}

	@Test
	void noAdminAddress() {
		var s1 = new MailService(mockSender, "alice@example.com", null);
		s1.sendToAdmin("网吧充钱提醒", "您的余额已不足请及时充值");
		Mockito.verify(mockSender, noInteractions()).send(any(MimeMessage.class));
	}

	@Test
	void sendToAdmin() throws Exception {
		service.sendToAdmin("网吧充钱提醒", "您的余额已不足请及时充值");

		var captor = ArgumentCaptor.forClass(MimeMessage.class);
		Mockito.verify(mockSender).send(captor.capture());
		var message = captor.getValue();

		var address = new InternetAddress();
		address.setAddress("bob@example.com");
		assertThat(message.getAllRecipients()).containsExactly(address);

		assertThat(message.getContent().toString()).contains("您的余额已不足请及时充值");
	}

	@Test
	void send() throws Exception {
		service.send("charlie@example.com", "网吧充钱提醒", "您有新的消息请注意查收");

		var captor = ArgumentCaptor.forClass(MimeMessage.class);
		Mockito.verify(mockSender).send(captor.capture());
		var message = captor.getValue();

		var address = new InternetAddress();
		address.setAddress("charlie@example.com");
		assertThat(message.getAllRecipients()).containsExactly(address);

		assertThat(message.getContent().toString()).contains("您有新的消息请注意查收");
	}
}
