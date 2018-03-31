package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.permission.PermissionKey;
import net.kaciras.blog.domain.permission.Role;
import net.kaciras.blog.domain.permission.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/roles")
public final class RoleController {

	private final RoleRepository roleRepository;

	@PostMapping
	public ResponseEntity<Void> addNewRole(@RequestParam String name) throws URISyntaxException {
		SecurtyContext.checkAccept("RoleService", "MODIFY");
		int id = roleRepository.add(name);
		return ResponseEntity.created(new URI("/api/role/" + id)).build();
	}

	@PutMapping("/{id}/includes")
	public ResponseEntity<Void> updateIncludes(@PathVariable int id,
											   @RequestParam List<Integer> includes) {
		SecurtyContext.checkAccept("RoleService", "MODIFY");
		List<Role> roles = includes.stream().map(roleRepository::get).collect(Collectors.toList());
		roleRepository.get(id).changeIncludes(roles);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRole(@PathVariable int id) {
		SecurtyContext.checkAccept("RoleService", "MODIFY");
		roleRepository.remove(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/permissions")
	public Set<PermissionKey> getPermissionFromRole(@PathVariable int id) {
		return roleRepository.get(id).getPermissionSet();
	}

	@PostMapping("/{id}/permissions")
	public ResponseEntity<Void> addPermissionToRole(@PathVariable int id, PermissionKey perm) {
		SecurtyContext.checkAccept("RoleService", "MODIFY");
		roleRepository.get(id).addPermission(perm);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/permission/{group}/{name}")
	public ResponseEntity<Void> deletePermissionFromRole(@PathVariable int id,
														 @PathVariable String group,
														 @PathVariable String name) {
		SecurtyContext.checkAccept("RoleService", "MODIFY");
		roleRepository.get(id).removePermission(new PermissionKey(group, name));
		return ResponseEntity.noContent().build();
	}
}
