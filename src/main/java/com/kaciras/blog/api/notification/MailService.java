package com.kaciras.blog.api.notification;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 由于我懒得搞邮箱验证，所以不支持给别人发邮件。
 */
@RequiredArgsConstructor
@Slf4j
public class MailService {

	private final JavaMailSender mailSender;
	private final Configuration freeMaker;

	private final String from;

	public void send(String to, String title, Object entry, String template) {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message);

		try (var out = new OutputStreamWriter(new ByteArrayOutputStream())) {
			helper.setTo(to);
			helper.setFrom(from, "KacirasBlog");
			helper.setSubject(title);

			freeMaker.getTemplate(template).process(entry, out);
			helper.setText(out.toString(), true);

			mailSender.send(message);
		} catch (IOException | MessagingException | TemplateException e) {
			logger.error("邮件发送失败", e);
		}
	}
}
