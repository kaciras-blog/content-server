package net.kaciras.blog.domain.permission;

import org.springframework.stereotype.Component;

@Component
public final class AuthenticatorFactory {

	private final RoleService roleService;
	private final PermissionRepository permissionRepository;

	public AuthenticatorFactory(RoleService roleService, PermissionRepository permissionRepository) {
		this.roleService = roleService;
		this.permissionRepository = permissionRepository;
	}

	public Authenticator create(String module) {
		return new DefaultAuthenticator(module, roleService, permissionRepository);
	}
}
