package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RESTful 不能很好地设计获取当前User的请求URL，故单独搞一个控制器。
 * 另一种设计是使用 /session/user，但账户服务也用了这个URL。
 */
@RequiredArgsConstructor
@RestController
class UserSessionController {

	private final UserMapper mapper;
	private final UserManager userManager;

	@GetMapping("/current-user")
	public UserVo getCurrentUser() {
		return mapper.toUserVo(userManager.ensureCurrent());
	}
}
