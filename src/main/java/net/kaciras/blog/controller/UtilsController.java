package net.kaciras.blog.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.CaptchaGenerator;
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

	@GetMapping("/captcha")
	public void getCaptcha(HttpSession session, HttpServletResponse resp) throws IOException {
		resp.setHeader("Pragma", "No-cache");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/jpeg");
		String captcha = captchaGenerator.writeCaptcha(resp.getOutputStream());
		session.setAttribute("Captcha", captcha.toLowerCase());
	}
}
