package net.kaciras.blog.api.misc;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SessionAttrNames;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Clock;

@RequiredArgsConstructor
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

	private final Clock clock;
	private final TextImageCaptchaGenerator generator;

	/**
	 * 生成验证码图片，并绑定会话属性。前端通常是一个img元素使用src属性发出请求。
	 *
	 * @param session 会话
	 * @param resp 响应
	 * @throws IOException 如果发生IO错误
	 */
	@GetMapping
	public void getCaptcha(HttpSession session, HttpServletResponse resp) throws IOException {
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/jpeg");

		var captcha = generator.generate(resp.getOutputStream());
		session.setAttribute(SessionAttrNames.CAPTCHA_SESSION_NAME, captcha);
		session.setAttribute(SessionAttrNames.CAPTCHA_SESSION_TIME, clock.millis());
	}
}
