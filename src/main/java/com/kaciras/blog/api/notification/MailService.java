package com.kaciras.blog.api.notification;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * 由于我懒得搞邮箱验证（不验证有滥用风险），所以不支持给别人发邮件。
 */
@RequiredArgsConstructor
@Slf4j
public class MailService {

	private final JavaMailSender mailSender;
	private final Configuration freeMarker;

	private final String from;

	/**
	 * 发送邮件，使用 FreeMarker 模板来生成邮件内容。
	 *
	 * @param to 邮件发到哪
	 * @param title 标题
	 * @param entry 模板的数据
	 * @param template 模板名
	 */
	public void send(String to, String title, Object entry, String template) {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message);

		try (var out = new StringWriter()) {
			helper.setTo(to);
			helper.setFrom(from, "KacirasBlog");
			helper.setSubject(title);

			freeMarker.getTemplate(template).process(entry, out);
			helper.setText(out.toString(), true);

			mailSender.send(message);
		} catch (IOException | MessagingException | TemplateException e) {
			logger.error("邮件发送失败", e);
		}
	}
}
