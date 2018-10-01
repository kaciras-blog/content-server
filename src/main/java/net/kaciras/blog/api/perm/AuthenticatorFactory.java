package net.kaciras.blog.api.perm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public final class AuthenticatorFactory {

	@Value("${debug-permission}")
	private boolean debugPermission;

	private final RestTemplate restTemplate;

	public Authenticator create(String module) {
		if(debugPermission) {
			return perm -> false;
		}
		return new DefaultAuthenticator(module, restTemplate);
	}
}
