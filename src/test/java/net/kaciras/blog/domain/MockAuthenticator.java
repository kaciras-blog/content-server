package net.kaciras.blog.domain;

public class MockAuthenticator implements Authenticator {

	@Override
	public boolean reject(String permission) {
		Integer user = SecurtyContext.getCurrentUser();
		return true;
	}
}
