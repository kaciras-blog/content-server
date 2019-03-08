package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principle.AuthType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GithubOauth2Client implements Oauth2Client {

	private static final String CLIENT_ID = "08878e62e2f3cb5be51b";
	private static final String CLIENT_SECRET = "04b1aebe5770a4458bfa17d79bc5cd1988dcba45";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Override
	public AuthType authType() {
		return AuthType.Github;
	}

	@Override
	public UriComponentsBuilder authUri() {
		return UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/authorize")
				.queryParam("client_id", CLIENT_ID)
				.queryParam("scope", "read:user")
				.queryParam("state", UUID.randomUUID().toString());
	}

	@Override
	public UserInfo getUserInfo(String code, @Nullable String state) throws Exception {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", CLIENT_ID)
				.queryParam("client_secret", CLIENT_SECRET)
				.queryParam("code", code)
				.queryParam("state", state)
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

		public final long id;
		public final String login;
		public final String name;
		public final String avatar_url;

		@Override
		public long id() {
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
