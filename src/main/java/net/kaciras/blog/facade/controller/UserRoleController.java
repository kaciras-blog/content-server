package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.permission.PermissionKey;
import net.kaciras.blog.domain.permission.RoleRepository;
import net.kaciras.blog.domain.permission.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public final class UserRoleController {

	private final RoleService roleService;
	private final RoleRepository roleRepository;

	//此方法用于HEAD请求
	@GetMapping("/{id}/allowances")
	public ResponseEntity<Void> getAllowances(@PathVariable int id, PermissionKey pk) {
		if (roleService.accept(id, pk))
			return ResponseEntity.ok().build();
		return ResponseEntity.notFound().build();
	}

//	@GetMapping("/{id}/roles")
//	public Set<Role> getUserRoles(@PathVariable int id) {
//		return roleService.getUser(id).getRoleSet();
//	}
//
//	@PostMapping("/{id}/roles")
//	public ResponseEntity<Void> addRoleToUser(@PathVariable int id, @RequestParam int role) {
//		roleService.getUser(id).addRole(roleRepository.get(role));
//		return ResponseEntity.noContent().build();
//	}
//
//	@DeleteMapping("/{id}/roles/{role}")
//	public ResponseEntity<Void> deleteRoleFromUser(@PathVariable int id, @PathVariable int role) {
//		roleService.getUser(id).removeRole(roleRepository.get(role));
//		return ResponseEntity.noContent().build();
//	}
}
