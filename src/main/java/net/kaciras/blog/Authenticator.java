package net.kaciras.blog;

import net.kaciras.blog.infrastructure.exception.PermissionException;

public interface Authenticator {

	boolean reject(String permission);

	default void require(String permission) {
		if (reject(permission)) throw new PermissionException();
	}
}
