package net.kaciras.blog.domain.permission;

import net.kaciras.blog.infrastructure.exception.PermissionException;

public interface Authenticator {

	boolean check(String permission);

	default void require(String permission) {
		if (!check(permission)) throw new PermissionException();
	}
}
