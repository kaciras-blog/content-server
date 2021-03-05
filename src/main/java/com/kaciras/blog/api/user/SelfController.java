package com.kaciras.blog.api.user;

import com.kaciras.blog.api.SessionValue;
import com.kaciras.blog.infra.principal.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * 当前登录的用户控制器，路径以简洁为主使用 /user 而不是 /session/user 之类的，跟 GitHub 一样。
 * <p>
 * 这里有注销但没有登录和注册，这俩功能见 account 包。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
class SelfController {

	private final UserRepository repository;
	private final UserMapper mapper;

	@GetMapping
	public UserVO get() {
		return mapper.toUserVo(repository.get(SecurityContext.getUserId()));
	}

	@DeleteMapping
	public ResponseEntity<Void> logout(HttpSession session) {
		SessionValue.USER_ID.removeFrom(session);
		return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
	}

	@PatchMapping
	public ResponseEntity<Void> update(@RequestBody @Valid UpdateDTO data) {
		SecurityContext.requireLogin();
		var user = repository.get(SecurityContext.getUserId());

		mapper.populate(user, data);
		repository.update(user);
		return ResponseEntity.noContent().build();
	}
}
