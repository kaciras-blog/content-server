package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequireAuthorize // 仅管理员
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
class UserController {

	private final UserManager userManager;

	@GetMapping("/{id}")
	public UserVo get(@PathVariable int id) {
		return userManager.getUser(id);
	}
}
