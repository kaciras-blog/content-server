package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.user.LoginVo;
import net.kaciras.blog.domain.user.User;
import net.kaciras.blog.domain.user.UserService;
import net.kaciras.blog.facade.pojo.PojoMapper;
import net.kaciras.blog.facade.pojo.UserVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/session")
public final class SessionController {

	private final UserService userService;
	private final PojoMapper mapper;

	@GetMapping("/user")
	public ResponseEntity<UserVO> getCurrentUser(HttpSession session, HttpServletRequest request) throws UnknownHostException {
		Integer id = (Integer) session.getAttribute("UserId");
		if (id == null) {
			return ResponseEntity.notFound().build();
		}

		//TODO:
		User user = userService.getUser(id);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		user.recordLogin(InetAddress.getByName(request.getRemoteAddr()));
		return ResponseEntity.ok(mapper.toUserVo(user));
	}

	@PostMapping("/user")
	public Map login(HttpServletRequest request, HttpServletResponse response,
					 @RequestBody @Valid LoginVo loginVo) {

		User user = userService.login(loginVo.getName(), loginVo.getPassword());
		if (user != null) {
			putUser(request, response, user, loginVo.isRemenber());
			return Map.of("message", "登录成功");
		} else {
			response.setStatus(400);
			return Map.of("message", "用户不存在或密码错误");
		}
	}

	private static Cookie copySessionCookie(HttpSession session) {
		session.setMaxInactiveInterval(30 * 24 * 60 * 60);
		Cookie cookie = new Cookie("JSESSIONID", session.getId());
		cookie.setPath("/");
		cookie.setMaxAge(session.getMaxInactiveInterval());
		cookie.setHttpOnly(true);
		return cookie;
	}

	@DeleteMapping("/user")
	public ResponseEntity logout(HttpSession session) {
		session.removeAttribute("UserId");
		return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
	}

	public static void putUser(HttpServletRequest request, HttpServletResponse response, User user, boolean remenber) {
		HttpSession session = request.getSession(true);
		String csrfToken = UUID.randomUUID().toString();
		session.setAttribute("UserId", user.getId());
		session.setAttribute("CSRF-Token", csrfToken);

		String domain = request.getServletContext().getSessionCookieConfig().getDomain();

		Cookie csrfCookie = new Cookie("CSRF-Token", csrfToken);
		csrfCookie.setPath("/");
		csrfCookie.setDomain(domain);

		if (remenber) {
			Cookie sessionCookie = copySessionCookie(session);
			sessionCookie.setDomain(domain);
			response.addCookie(sessionCookie);
			csrfCookie.setMaxAge(session.getMaxInactiveInterval());
		}
		response.addCookie(csrfCookie);
	}
}
