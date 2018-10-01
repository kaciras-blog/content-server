package net.kaciras.blog.domain;

import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.perm.Authenticator;

public class MockAuthenticator implements Authenticator {

	@Override
	public boolean reject(String permission) {
		Integer user = SecurtyContext.getCurrentUser();
		return true;
	}
}
