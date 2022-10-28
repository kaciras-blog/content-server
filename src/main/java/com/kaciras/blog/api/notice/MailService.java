package com.kaciras.blog.api.notice;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 可选的发邮件服务，如果没有配置邮件则该 Bean 不存在。
 *
 * <h2>安全性</h2>
 * 给别人发邮件前一定要先验证地址，否则可能被利用作为邮件轰炸机之类的导致被拉黑。
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MailService {

	private final Pattern placeholder = Pattern.compile("%(\\w+)%");

	private final JavaMailSender mailSender;
	private final String from;
	private final String adminAddress;

	@Value("${app.name}")
	private String name;

	/**
	 * 通过填充模板来生成 HTML，目前的邮件都比较简单，就不上专门的模板引擎了。
	 * ……好像简单的 if 功能也挺有用的。
	 *
	 * <h2>未来的想法</h2>
	 * 如果用 Node 做同构应用，这玩意也能用 SSR 生成。
	 *
	 * @param name  模板名，位于 resources/mail 下，扩展名省略。
	 * @param model 填充参数
	 * @return 填充后的 HTML，可作为邮件内容。
	 */
	@SneakyThrows
	public String interpolate(String name, Map<String, Object> model) {
		var resource = new ClassPathResource("mail/" + name + ".html");

		/*
		 * 不能用 Files.readString(Path.of(...))，否则打包后运行出错。
		 * https://stackoverflow.com/a/25033217/7065321
		 */
		@Cleanup var stream = resource.getInputStream();
		var template = new String(stream.readAllBytes());
		return placeholder.matcher(template)
				.replaceAll(m -> model.get(m.group(1)).toString());
	}

	/**
	 * 发送一封邮件给博主，如果博主没有设置自己的邮件地址则什么也不做。
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
	 * 向指定的邮箱发送一封邮件。
	 *
	 * @param to    邮件发到哪
	 * @param title 标题
	 * @param html  内容，HTML格式
	 */
	public void send(String to, String title, String html) {
		var message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message);

		try {
			helper.setFrom(from, name);
			helper.setTo(to);
			helper.setSubject(title + " - " + name);
			helper.setText(html, true);

			mailSender.send(message);
		} catch (IOException | MessagingException e) {
			logger.error("邮件发送失败，To = {}，Title = {}", to, title, e);
		}
	}
}
