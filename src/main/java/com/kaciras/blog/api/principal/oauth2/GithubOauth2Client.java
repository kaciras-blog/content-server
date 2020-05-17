package com.kaciras.blog.api.principal.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.principal.AuthType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * https://developer.github.com/apps/building-oauth-apps/authorizing-oauth-apps/
 */
@ConditionalOnProperty("app.oauth2.github.client-secret")
@Component
@RequiredArgsConstructor
public final class GithubOauth2Client implements Oauth2Client {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Value("${app.oauth2.github.client-id}")
	private String clientId;

	@Value("${app.oauth2.github.client-secret}")
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
	public UserInfo getUserInfo(OAuth2Context context) throws Exception {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", context.getCode())
				.queryParam("state", context.getState())
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

	/*
	 * 构造方法只有一个参数的时候，JsonCreator默认的策略采用Mode.DELEGATING模式，而多个
	 * 参数的情况下才是Mode.PROPERTIES。
	 * 这里只有一个参数，所以需要显示指定Mode.PROPERTIES模式
	 */
	@AllArgsConstructor(onConstructor_ = @JsonCreator(mode = Mode.PROPERTIES))
	private static final class AccessTokenEntity {
		private final String access_token;
//		private final String scope;
//		private final String token_type;
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class UserProfile implements UserInfo {

		private final String id;
		private final String login;
		private final String name;
		private final String avatar_url;

		@Override
		public String id() {
			return id;
		}

		@Override
		public String name() {
			return name != null ? name : login;
		}

		@Override
		public String avatar() {
			return avatar_url;
		}
	}
}
