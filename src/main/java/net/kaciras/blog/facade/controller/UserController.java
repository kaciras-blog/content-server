package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.user.RegisterVo;
import net.kaciras.blog.domain.user.User;
import net.kaciras.blog.domain.user.UserService;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public final class UserController {

	private final UserService userService;

	@GetMapping("/{id}")
	public String get(@PathVariable int id) {
		return "{}";
	}

	@PostMapping
	public ResponseEntity post(HttpServletRequest request,
							   HttpServletResponse response,
							   @Valid @RequestBody RegisterVo dto) throws UnknownHostException {
		HttpSession session = request.getSession(true);

		String checkCap = (String) session.getAttribute("Captcha");
		session.removeAttribute("Captcha"); //验证码是一次性的，记得移除
		if (checkCap == null || !checkCap.equals(dto.getCaptcha())) {
			throw new RequestArgumentException("验证码错误");
		}

		try {
			dto.setRegAddress(InetAddress.getByName(request.getRemoteAddr()));
			User user = userService.register(dto);
			SessionController.putUser(request, response, user, false);
			return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
		} catch (IllegalArgumentException ex) {
			throw new RequestArgumentException(ex.getMessage());
		}
	}

	@PutMapping("/{id}/availability")
	public ResponseEntity setAvailability(@PathVariable int id,
										  @RequestParam boolean banned,
										  @RequestParam(required = false, defaultValue = "0") long time,
										  @RequestParam(required = false, defaultValue = "0") int bid,
										  @RequestParam String cause) {
		if(banned) {
			userService.ban(id, time, cause);
		} else {
			userService.unban(id, bid, cause);
		}
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity delete(@PathVariable int id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
