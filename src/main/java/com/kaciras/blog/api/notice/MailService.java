package com.kaciras.blog.api.notice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import java.io.IOException;

/**
 * 可选的发邮件服务，如果没有配置邮件则该 Bean 不存在。
 *
 * <h2>安全性</h2>
 * 给别人发邮件前一定要先验证地址，否则可能被利用作为邮件轰炸机之类的导致被拉黑。
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MailService {

	private final JavaMailSender mailSender;
	private final String from;
	private final String adminAddress;

	/**
	 * 发送一个邮件给博主，如果博主没有设置自己的邮件地址则什么也不做。
	 *
	 * @param title 标题
	 * @param html  内容，HTML格式
	 */
	public void sendToAdmin(String title, String html) {
		if (adminAddress != null) {
			send(adminAddress, title, html);
		}
	}

	/**
	 * 发送邮件。
	 *
	 * @param to    邮件发到哪
	 * @param title 标题
	 * @param html  内容，HTML格式
	 */
	public void send(String to, String title, String html) {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message);

		try {
			helper.setFrom(from, "KacirasBlog");
			helper.setTo(to);
			helper.setSubject(title);

			/*
			 * 邮件内容也属于前端视图，但它由后端发送，所以只能选个后端模板库用了。
			 * 而且邮件环境不同于浏览器，没法复用前端项目代码。
			 *
			 * 可否用模板生成 Markdown，然后再渲染成 HTML？这样做好像仍然需要模板引擎。
			 *
			 * 【最简化邮件内容】
			 * 因为不给第三方发邮件，自己的话可以在控制台里看到全部消息通知，
			 * 完全没必在再邮件里写内容，只需提个醒去控制台看即可，故移除后端模板。
			 */
			helper.setText(html, true);

			mailSender.send(message);
		} catch (IOException | MessagingException e) {
			logger.error("邮件发送失败", e);
		}
	}
}
