package com.kaciras.blog.api.account.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.account.AuthType;
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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * https://developers.google.com/identity/protocols/OpenIDConnect
 */
@ConditionalOnProperty(prefix = "app.oauth2.google", value = {"client-id", "client-secret"})
@Component
@RequiredArgsConstructor
public final class GoogleOAuth2Client implements OAuth2Client {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Value("${app.oauth2.google.client-id}")
	private String clientId;

	@Value("${app.oauth2.google.client-secret}")
	private String clientSecret;

	@Override
	public AuthType authType() {
		return AuthType.Google;
	}

	@Override
	public UriComponentsBuilder uriTemplate() {
		return UriComponentsBuilder
				.fromUriString("https://accounts.google.com/o/oauth2/auth")
				.queryParam("client_id", clientId)
				.queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile")
				.queryParam("response_type", "code")
				.queryParam("access_type", "offline");
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public UserProfile getUserInfo(OAuth2Context context) throws Exception {
		var formParams = UriComponentsBuilder.newInstance()
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", context.getCode())
				.queryParam("redirect_uri", context.getCurrentUri())
				.queryParam("grant_type", "authorization_code");

		var request = HttpRequest
				.newBuilder(URI.create("https://www.googleapis.com/oauth2/v4/token"))
				.POST(BodyPublishers.ofString(formParams.build().getQuery()))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();

		var res = httpClient.send(request, BodyHandlers.ofString());
		if (res.statusCode() != 200) {
			throw new Error("OAuth Error" + res.body());
		}

		var tokenEntity = objectMapper.readValue(res.body(), AccessTokenEntity.class);
		return getUserProfile(tokenEntity.access_token);
	}

	private UserProfile getUserProfile(String accessToken) throws IOException, InterruptedException {
		var uu = UriComponentsBuilder
				.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
				.queryParam("fields", "id,name,picture")
				.build().toUri();

		var request = HttpRequest.newBuilder(uu)
				.header("Authorization", "Bearer " + accessToken)
				.build();

		var res = httpClient.send(request, BodyHandlers.ofString());
		return objectMapper.readValue(res.body(), GoogleUserProfile.class);
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator(mode = Mode.PROPERTIES))
	private static final class AccessTokenEntity {
		private final String access_token;
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class GoogleUserProfile implements UserProfile {

		/** 谷歌的ID特别长，不能用整数类型 */
		private final String id;
		private final String name;
		private final String email;
		private final String picture;

		@Override
		public String id() {
			return id;
		}

		@Override
		public String avatar() {
			return picture;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String email() {
			return email;
		}
	}
}
