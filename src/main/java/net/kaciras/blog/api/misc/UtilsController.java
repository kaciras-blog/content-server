package net.kaciras.blog.api.misc;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/utils")
final class UtilsController {

	private final CaptchaGenerator captchaGenerator;

	/**
	 * 生成验证码图片，并绑定会话属性。前端通常是一个img元素使用src属性发出请求。
	 * 相应头已包含了缓存控制，无需再url后面加随机数。
	 *
	 * @param session 会话
	 * @param resp 响应
	 * @throws IOException 如果发生IO错误
	 */
	@GetMapping("/captcha")
	public void getCaptcha(HttpSession session, HttpServletResponse resp) throws IOException {
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/jpeg");

		var captcha = captchaGenerator.generateCaptchaTo(resp.getOutputStream());
		session.setAttribute("Captcha", captcha.toLowerCase());
	}
}
