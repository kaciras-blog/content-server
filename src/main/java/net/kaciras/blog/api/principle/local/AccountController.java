package net.kaciras.blog.api.principle.local;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SessionAttrNames;
import net.kaciras.blog.api.principle.SessionService;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.Clock;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

	private static final long CAPTCHA_LIFE_TIME = 5 * 60 * 1000;

	private final Clock clock;

	private final AccountRepository repository;
	private final SessionService sessionService;

	@PostMapping
	public ResponseEntity post(HttpServletRequest request, HttpServletResponse response,
							   @Valid @RequestBody RegisterRequest dto) throws UnknownHostException {
		var session = request.getSession(true);

		// 检查验证码。注意验证码是一次性的，记得移除
		var checkCap = session.getAttribute("Captcha");
		session.removeAttribute("Captcha");
		if (checkCap == null || !checkCap.equals(dto.getCaptcha())) {
			throw new RequestArgumentException("验证码错误");
		}

		var account = Account.create(dto.getName(), dto.getPassword());
		account.setRegisterAddress(InetAddress.getByName(request.getRemoteAddr()));

		try {
			repository.add(account);
			sessionService.putUser(request, response, account.getId(), false);
			return ResponseEntity.created(URI.create("/accounts/" + account.getId())).build();
		} catch (SQLException e) {
			throw new RequestArgumentException("用户名已被使用");
		}
	}

	/**
	 * 检查用户输入的验证码是否正确且在有效期内。
	 * [注意] 会话中的验证码是一次性的，在该方法里会被移除。
	 *
	 * @param session 会话
	 * @param value 用户输入的验证码
	 * @throws RequestArgumentException 如果检查出错误
	 */
	private void checkCaptcha(WebSession session, @NonNull String value) {
		var attrs = session.getAttributes();

		var except = attrs.remove(SessionAttrNames.CAPTCHA_SESSION_NAME);
		if (!value.equals(except)) {
			throw new RequestArgumentException("验证码错误");
		}

		var time = (long) attrs.get(SessionAttrNames.CAPTCHA_SESSION_TIME);
		if (clock.millis() - time > CAPTCHA_LIFE_TIME) {
			throw new RequestArgumentException("验证码已过期，请重试");
		}
	}

	@PostMapping("/login")
	public ResponseEntity<Void> login(HttpServletRequest request,
									  HttpServletResponse response,
									  @RequestBody @Valid LoginRequest loginVo) throws Exception {
		var account = repository.findByName(loginVo.getName());

		if (account == null || !account.checkLogin(loginVo.getPassword())) {
			throw new RequestArgumentException("密码错误或用户不存在");
		} else {
			sessionService.putUser(request, response, account.getId(), loginVo.isRemember());
			return ResponseEntity.created(URI.create("/session/user")).build();
		}
	}
}
