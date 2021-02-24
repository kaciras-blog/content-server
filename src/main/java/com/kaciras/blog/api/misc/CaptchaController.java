package com.kaciras.blog.api.misc;

import com.kaciras.blog.api.SessionAttributes;
import lombok.RequiredArgsConstructor;
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
	 * @param resp    响应
	 * @throws IOException 如果发生IO错误
	 */
	@GetMapping
	public void getCaptcha(HttpSession session, HttpServletResponse resp) throws IOException {
		resp.setContentType("image/jpeg");

		/*
		 * Cache-Control：
		 *  	no-cache 并非不缓存的意思（这名字就是一个错误）而是即使没过期也要发送校验请求，
		 *  	must-revalidate 缓存过期前可以直接用，过期了再去校验
		 *  	no-store 这个才是真正的不缓存。
		 *
		 * 虽然 no-store 一个即可禁止缓存，但考虑到兼容性还是把三个都写上。
		 * Expires 设置过期时间，一些代理可能会依赖这个头所以加上，即便在Cache-Control里已经禁止了缓存。
		 */
		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setDateHeader("Expires", 0);

		var captcha = generator.generate(resp.getOutputStream());
		session.setAttribute(SessionAttributes.CAPTCHA, captcha);
		session.setAttribute(SessionAttributes.CAPTCHA_TIME, clock.instant());
	}
}
