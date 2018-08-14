package net.kaciras.blog.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public final class AuthenticatorFactory {

	private final RestTemplate restTemplate;

	public Authenticator create(String module) {
		return new DefaultAuthenticator(module, restTemplate);
	}
}
