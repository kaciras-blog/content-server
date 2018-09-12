package net.kaciras.blog.domain;

import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.SecurtyContext;

public class MockAuthenticator implements Authenticator {

	@Override
	public boolean reject(String permission) {
		Integer user = SecurtyContext.getCurrentUser();
		return true;
	}
}
