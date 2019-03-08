package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principle.AuthType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

@RequiredArgsConstructor
@Component
public class GithubOauth2Client implements Oauth2Client {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Value("${kaciras.oauth.github.client-id}")
	private String clientId;

	@Value("${kaciras.oauth.github.client-secret}")
	private String clientSecret;

	@Override
	public AuthType authType() {
		return AuthType.Github;
	}

	@Override
	public UriComponentsBuilder authUri() {
		return UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/authorize")
				.queryParam("client_id", clientId)
				.queryParam("scope", "read:user")
				.queryParam("response_type", "code");
	}

	@Override
	public UserInfo getUserInfo(AuthContext context) throws Exception {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", context.code)
				.queryParam("state", context.state)
				.build().toUri();

		var request = HttpRequest.newBuilder(authUri)
				.header("Accept", "application/json")
				.build();

		var res = httpClient.send(request, BodyHandlers.ofString());
		if (res.statusCode() != 200) {
			throw new IOException("Oauth获取AccessToken失败，返回码：" + res.statusCode());
		}
		var token = objectMapper.readValue(res.body(), AccessTokenEntity.class);
		return getUserProfile(token.access_token);
	}

	private UserProfile getUserProfile(String accessToken) throws IOException, InterruptedException {
		var request = HttpRequest
				.newBuilder(URI.create("https://api.github.com/user"))
				.header("Accept", "application/json")
				.header("Authorization", "token " + accessToken)
				.build();
		var res = httpClient.send(request, BodyHandlers.ofString());

		if (res.statusCode() != 200) {
			throw new IOException("获取用户信息失败，返回码：" + res.statusCode());
		}
		return objectMapper.readValue(res.body(), UserProfile.class);
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class AccessTokenEntity {
		public final String access_token;
		public final String scope;
		public final String token_type;
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class UserProfile implements UserInfo {

		public final String id;
		public final String login;
		public final String name;
		public final String avatar_url;

		@Override
		public String id() {
			return id;
		}

		@Override
		public String name() {
			return name != null ? name : login;
		}

		@Override
		public String head() {
			return avatar_url;
		}
	}
}
