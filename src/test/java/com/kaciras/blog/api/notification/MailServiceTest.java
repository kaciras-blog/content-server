package com.kaciras.blog.api.notification;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import javax.mail.internet.MimeMessage;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

final class MailServiceTest {

	private final JavaMailSender mockSender = Mockito.mock(JavaMailSender.class);

	@Test
	void send() throws Exception {
		Mockito.when(mockSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());

		var freeMarker = new Configuration(Configuration.VERSION_2_3_30);
		freeMarker.setTemplateLoader(new SpringTemplateLoader(new DefaultResourceLoader(), "classpath:/templates"));
		freeMarker.setDefaultEncoding("utf-8");

		var service = new MailService(mockSender, freeMarker, "test@example.com");

		var entry = new DiscussionActivity();
		entry.setTitle("Test Article");
		entry.setPreview("test");
		entry.setTime(Instant.now());
		entry.setUrl("https://example.com");
		entry.setFloor(7);

		service.send("test@example.com", "title", entry, "discussion-mail.ftl");

		var c = ArgumentCaptor.forClass(MimeMessage.class);
		Mockito.verify(mockSender).send(c.capture());
		assertThat(c.getValue().getContent().toString()).contains("<p>该评论位于第7楼</p>");
	}
}
