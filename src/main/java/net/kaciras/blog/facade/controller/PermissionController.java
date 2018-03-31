package net.kaciras.blog.facade.controller;

import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public final class PermissionController {

	@GetMapping("{group}/{name}/verification")
	public ResponseEntity<Void> verifyPermission(@PathVariable String group,
												 @PathVariable String name) {
		boolean accept = SecurtyContext.accept(group, name);
		return accept ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
	}
}
