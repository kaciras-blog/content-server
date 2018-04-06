package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.permission.PermissionKey;
import net.kaciras.blog.domain.permission.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/permissions")
public final class PermissionController {

	private final RoleService roleService;

	@GetMapping("{group}/{name}/verification")
	public ResponseEntity<Void> verifyPermission(@PathVariable String group,
												 @PathVariable String name) {
		boolean accept = roleService.accept(SecurtyContext.getCurrentUser(), new PermissionKey(group, name));
		return accept ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
	}
}
