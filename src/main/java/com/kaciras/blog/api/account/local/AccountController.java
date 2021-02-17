package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.SessionAttributes;
import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.api.account.SessionService;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.infra.RequestUtils;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.URI;
import java.sql.SQLException;
import java.time.Clock;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
class AccountController {

	/** 验证码过期时间（毫秒） */
	private static final long CAPTCHA_EXPIRE = 5 * 60 * 1000;

	private final Clock clock;

	private final AccountRepository repository;
	private final SessionService sessionService;
	private final UserManager userManager;

	@PostMapping
	public ResponseEntity<Void> post(@Valid @RequestBody RegisterDTO data,
									 HttpServletRequest request,
									 HttpServletResponse response) {
		checkCaptcha(request.getSession(true), data.captcha);

		var account = createAccount(data, RequestUtils.addressFromRequest(request));
		sessionService.putUser(request, response, account.getId(), true);
		return ResponseEntity.created(URI.create("/accounts/" + account.getId())).build();
	}

	@Transactional
	protected Account createAccount(RegisterDTO data, InetAddress ip) {
		try {
			var id = userManager.createNew(data.name, AuthType.LOCAL, ip);
			var account = Account.create(id, data.name, data.password);
			repository.add(account);
			return account;
		} catch (SQLException e) {
			throw new RequestArgumentException("用户名已被使用");
		}
	}

	/**
	 * 检查用户输入的验证码是否正确且在有效期内。
	 * 【注意】会话中的验证码是一次性的，在该方法里会被移除。
	 *
	 * @param session 会话
	 * @param value   用户输入的验证码
	 * @throws RequestArgumentException 如果检查出错误
	 */
	private void checkCaptcha(HttpSession session, @NonNull String value) {
		var except = session.getAttribute(SessionAttributes.CAPTCHA);
		session.removeAttribute(SessionAttributes.CAPTCHA);
		if (!value.equals(except)) {
			throw new RequestArgumentException("验证码错误");
		}

		var time = (long) session.getAttribute(SessionAttributes.CAPTCHA_TIME);
		if (clock.millis() - time > CAPTCHA_EXPIRE) {
			throw new RequestArgumentException("验证码已过期，请重试");
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginDTO data,
								   HttpServletRequest request,
								   HttpServletResponse response) {
		var account = repository.findByName(data.name);

		if (account == null || !account.checkLogin(data.password)) {
			throw new RequestArgumentException("密码错误或用户不存在");
		}
		sessionService.putUser(request, response, account.getId(), data.remember);
		return ResponseEntity.created(URI.create("/session/user")).build();
	}
}
