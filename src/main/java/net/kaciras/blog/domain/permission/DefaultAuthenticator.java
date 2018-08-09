package net.kaciras.blog.domain.permission;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public final class DefaultAuthenticator implements Authenticator {

	private final String module;
	private final RestTemplate restTemplate;

	@Override
	public boolean reject(String permission) {
		var userId = SecurtyContext.getCurrentUser();
		if(userId == null) {
			userId = 0;
		}
		var status = restTemplate.execute("http://localhost:26480/accounts/{id}/prems/{module}/{name}",
				HttpMethod.HEAD, null, ClientHttpResponse::getStatusCode, userId, module, permission);
		return status != HttpStatus.OK;
	}
}
