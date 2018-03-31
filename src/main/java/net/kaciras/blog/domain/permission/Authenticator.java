package net.kaciras.blog.domain.permission;

import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.infrastructure.exception.PermissionException;

public final class Authenticator {

	private final RoleService roleService;
	private final PermissionRepository permissionRepository;

	private final String module;

	Authenticator(String module, RoleService roleService,
				  PermissionRepository permissionRepository) {
		this.module = module;
		this.roleService = roleService;
		this.permissionRepository = permissionRepository;
	}

	public void require(String permission) {
		if (!check(permission)) throw new PermissionException();
	}

	public boolean check(String permission) {
		PermissionKey key = new PermissionKey(module, permission);
		if (!permissionRepository.contains(key)) {
			throw new Error("使用了未定义的权限");
		}
		return roleService.accept(SecurtyContext.getCurrentUser(), key);
	}
}
