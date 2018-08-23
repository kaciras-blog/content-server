package net.kaciras.blog.domain;

import net.kaciras.blog.Authenticator;
import net.kaciras.blog.SecurtyContext;

public class MockAuthenticator implements Authenticator {

	@Override
	public boolean reject(String permission) {
		Integer user = SecurtyContext.getCurrentUser();
		return true;
	}
}
