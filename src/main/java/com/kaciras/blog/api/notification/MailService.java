package com.kaciras.blog.api.notification;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 由于我懒得搞邮箱验证，所以不支持给别人发邮件。
 */
@Slf4j
@ConditionalOnProperty("app.mail-notify.send-to")
@Service
public class MailService {

	private final JavaMailSender mailSender;
	private final Configuration freemaker;

	@Value("${app.mail-notify.from}")
	private String from;

	public MailService(JavaMailSender mailSender) throws IOException {
		this.mailSender = mailSender;

		freemaker = new Configuration(Configuration.getVersion());
		freemaker.setDirectoryForTemplateLoading(new ClassPathResource("mail-templates").getFile());
		freemaker.setDefaultEncoding("utf-8");
	}

	public void send(String to, String title, Object entry, String template) {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message);

		try (var out = new OutputStreamWriter(new ByteArrayOutputStream())) {
			helper.setTo(to);
			helper.setFrom(from, "KacirasBlog");
			helper.setSubject(title);

			freemaker.getTemplate(template).process(entry, out);
			helper.setText(out.toString(), true);

			mailSender.send(message);
		} catch (IOException | MessagingException | TemplateException e) {
			logger.error("邮件发送失败", e);
		}
	}
}
