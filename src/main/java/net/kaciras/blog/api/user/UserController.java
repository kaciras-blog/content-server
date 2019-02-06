package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequireAuthorize // 仅管理员
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
class UserController {

	private final UserRepository repository;
	private final UserManager userManager;

	@GetMapping("/{id}")
	public UserVo get(@PathVariable int id) {
		return userManager.getUser(id);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Void> patch(@PathVariable int id, @RequestBody PatchMap patchMap) {
		var user = repository.get(id);

		if (patchMap.getHead() != null) {
			user.updateHead(patchMap.getHead());
		}
		return ResponseEntity.noContent().build();
	}
}
