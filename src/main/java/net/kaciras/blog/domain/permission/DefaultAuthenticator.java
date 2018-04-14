package net.kaciras.blog.domain.permission;

import net.kaciras.blog.domain.SecurtyContext;

public final class DefaultAuthenticator implements Authenticator {

	private final RoleService roleService;
	private final PermissionRepository permissionRepository;

	private final String module;

	DefaultAuthenticator(String module, RoleService roleService,
						 PermissionRepository permissionRepository) {
		this.module = module;
		this.roleService = roleService;
		this.permissionRepository = permissionRepository;
	}

	@Override
	public boolean check(String permission) {
		PermissionKey key = new PermissionKey(module, permission);
		if (!permissionRepository.contains(key)) {
			throw new Error("使用了未定义的权限");
		}
		return roleService.accept(SecurtyContext.getCurrentUser(), key);
	}
}
